/**
 * SmartSpend AI Assistant — Powered by Gemini
 * Floating chat widget that calls the Gemini API directly from the browser.
 */

const GEMINI_API_KEY = 'AIzaSyBinxPZ39BGzJ11nJHxGu0_wMl4NyDR788';
const GEMINI_API_URL = `https://generativelanguage.googleapis.com/v1/models/gemini-1.5-flash:generateContent?key=${GEMINI_API_KEY}`;

const SYSTEM_PROMPT = `You are an intelligent AI assistant integrated into a web application called "Smart Spend".

Your role is to help users understand and use the platform efficiently. The platform helps users manage expenses, track spending, analyze budgets, and make smarter financial decisions.

Behavior Guidelines:
- Be polite, concise, and helpful.
- Always respond in simple and clear language.
- If the user is confused, guide them step-by-step.
- If the user asks about features, explain them practically.
- If the user shares financial data, give meaningful insights.
- Avoid long paragraphs; keep answers short and structured.

Core Capabilities:
1. Help users add, edit, or track expenses.
2. Explain dashboards, charts, and analytics.
3. Suggest budget improvements.
4. Provide smart financial tips based on user behavior.
5. Answer FAQs about the platform.

Context Awareness:
- Assume user is inside the Smart Spend dashboard.
- If user asks "how to add expense", guide UI steps.
- If user asks "why spending high", suggest analysis-based answers.
- If user asks general finance questions, give practical advice.

Tone:
- Friendly but professional
- Slightly conversational
- Not robotic

Fallback Rule:
If you don't know something, say:
"I'm not fully sure about that, but I can help you explore it step by step."

Important:
- Never mention you are Gemini or an API.
- Always act as an in-app assistant named "SpendBot".
- Keep responses concise and structured with bullet points when listing steps.`;

// ── Conversation history for multi-turn ──────────────────────────────────────
const conversationHistory = [];

// ── Build the widget HTML ─────────────────────────────────────────────────────
function createChatWidget() {
    const widget = document.createElement('div');
    widget.id = 'ss-chat-widget';
    widget.innerHTML = `
        <!-- Toggle bubble -->
        <button id="ss-chat-toggle" aria-label="Open SmartSpend Assistant" title="Ask SpendBot">
            <i class="bi bi-robot" id="ss-chat-icon-open"></i>
            <i class="bi bi-x-lg"  id="ss-chat-icon-close" style="display:none;"></i>
        </button>

        <!-- Chat panel -->
        <div id="ss-chat-panel" class="ss-chat-hidden" aria-live="polite">
            <div id="ss-chat-header">
                <div class="ss-chat-header-info">
                    <div class="ss-chat-avatar"><i class="bi bi-robot"></i></div>
                    <div>
                        <div class="ss-chat-title">SpendBot</div>
                        <div class="ss-chat-subtitle">SmartSpend AI Assistant</div>
                    </div>
                </div>
                <button class="ss-chat-close-btn" onclick="toggleChat()" aria-label="Close chat">
                    <i class="bi bi-x-lg"></i>
                </button>
            </div>

            <div id="ss-chat-messages">
                <!-- Welcome message -->
                <div class="ss-msg ss-msg-bot">
                    <div class="ss-msg-avatar"><i class="bi bi-robot"></i></div>
                    <div class="ss-msg-bubble">
                        👋 Hi! I'm <strong>SpendBot</strong>, your SmartSpend assistant.<br><br>
                        I can help you track expenses, understand your budget, or give you smart savings tips. What can I do for you today?
                    </div>
                </div>
            </div>

            <!-- Quick prompts -->
            <div id="ss-quick-prompts">
                <button class="ss-quick-btn" onclick="sendQuick('How do I add an expense?')">➕ Add expense</button>
                <button class="ss-quick-btn" onclick="sendQuick('Give me savings tips')">💡 Savings tips</button>
                <button class="ss-quick-btn" onclick="sendQuick('Explain the dashboard')">📊 Dashboard</button>
                <button class="ss-quick-btn" onclick="sendQuick('How do budgets work?')">💰 Budgets</button>
            </div>

            <div id="ss-chat-input-area">
                <textarea
                    id="ss-chat-input"
                    placeholder="Ask me anything about SmartSpend…"
                    rows="1"
                    onkeydown="handleChatKey(event)"
                    oninput="autoResize(this)"
                    aria-label="Chat message input"
                ></textarea>
                <button id="ss-send-btn" onclick="sendMessage()" aria-label="Send message">
                    <i class="bi bi-send-fill"></i>
                </button>
            </div>
        </div>
    `;
    document.body.appendChild(widget);
}

// ── Toggle open/close ────────────────────────────────────────────────────────
function toggleChat() {
    const panel  = document.getElementById('ss-chat-panel');
    const iconO  = document.getElementById('ss-chat-icon-open');
    const iconC  = document.getElementById('ss-chat-icon-close');
    const isOpen = !panel.classList.contains('ss-chat-hidden');

    if (isOpen) {
        panel.classList.add('ss-chat-hidden');
        iconO.style.display = '';
        iconC.style.display = 'none';
    } else {
        panel.classList.remove('ss-chat-hidden');
        iconO.style.display = 'none';
        iconC.style.display = '';
        document.getElementById('ss-chat-input').focus();
        scrollToBottom();
    }
}

document.addEventListener('DOMContentLoaded', () => {
    createChatWidget();
    document.getElementById('ss-chat-toggle').addEventListener('click', toggleChat);
});

// ── Send quick prompt ────────────────────────────────────────────────────────
function sendQuick(text) {
    // Hide quick prompts after first use
    const qp = document.getElementById('ss-quick-prompts');
    if (qp) qp.style.display = 'none';
    sendMessage(text);
}

// ── Handle Enter key ─────────────────────────────────────────────────────────
function handleChatKey(e) {
    if (e.key === 'Enter' && !e.shiftKey) {
        e.preventDefault();
        sendMessage();
    }
}

// ── Auto-resize textarea ──────────────────────────────────────────────────────
function autoResize(el) {
    el.style.height = 'auto';
    el.style.height = Math.min(el.scrollHeight, 120) + 'px';
}

// ── Scroll messages to bottom ─────────────────────────────────────────────────
function scrollToBottom() {
    const msgs = document.getElementById('ss-chat-messages');
    if (msgs) msgs.scrollTop = msgs.scrollHeight;
}

// ── Append a message bubble ───────────────────────────────────────────────────
function appendMessage(role, text, isHTML = false) {
    const msgs      = document.getElementById('ss-chat-messages');
    const wrapper   = document.createElement('div');
    wrapper.className = `ss-msg ${role === 'user' ? 'ss-msg-user' : 'ss-msg-bot'}`;

    if (role === 'bot') {
        wrapper.innerHTML = `
            <div class="ss-msg-avatar"><i class="bi bi-robot"></i></div>
            <div class="ss-msg-bubble">${isHTML ? text : escapeHtml(text)}</div>`;
    } else {
        wrapper.innerHTML = `
            <div class="ss-msg-bubble">${escapeHtml(text)}</div>
            <div class="ss-msg-avatar ss-msg-avatar-user"><i class="bi bi-person-fill"></i></div>`;
    }

    msgs.appendChild(wrapper);
    scrollToBottom();
    return wrapper;
}

// ── Typing indicator ──────────────────────────────────────────────────────────
function appendTyping() {
    const msgs    = document.getElementById('ss-chat-messages');
    const div     = document.createElement('div');
    div.className = 'ss-msg ss-msg-bot ss-typing-wrapper';
    div.id        = 'ss-typing';
    div.innerHTML = `
        <div class="ss-msg-avatar"><i class="bi bi-robot"></i></div>
        <div class="ss-msg-bubble ss-typing">
            <span></span><span></span><span></span>
        </div>`;
    msgs.appendChild(div);
    scrollToBottom();
}

function removeTyping() {
    const t = document.getElementById('ss-typing');
    if (t) t.remove();
}

// ── Escape HTML to prevent XSS ────────────────────────────────────────────────
function escapeHtml(text) {
    return text
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#039;')
        .replace(/\n/g, '<br>');
}

// ── Format bot response (basic markdown-lite) ─────────────────────────────────
function formatBotText(text) {
    return text
        // Bold **text**
        .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
        // Italic *text*
        .replace(/\*(.*?)\*/g, '<em>$1</em>')
        // Bullet points
        .replace(/^[-•]\s+(.+)$/gm, '<li>$1</li>')
        // Wrap consecutive <li> in <ul>
        .replace(/((<li>.*<\/li>\n?)+)/g, '<ul>$1</ul>')
        // Numbered lists
        .replace(/^\d+\.\s+(.+)$/gm, '<li>$1</li>')
        // Newlines to breaks
        .replace(/\n/g, '<br>');
}

// ── Main send function ─────────────────────────────────────────────────────────
async function sendMessage(quickText) {
    const input   = document.getElementById('ss-chat-input');
    const sendBtn = document.getElementById('ss-send-btn');
    const text    = (quickText || input.value).trim();

    if (!text) return;

    // Hide quick prompts
    const qp = document.getElementById('ss-quick-prompts');
    if (qp) qp.style.display = 'none';

    // Clear input
    if (!quickText) {
        input.value = '';
        input.style.height = 'auto';
    }

    // Disable send while processing
    sendBtn.disabled = true;

    // Append user message
    appendMessage('user', text);

    // Add to history
    conversationHistory.push({ role: 'user', parts: [{ text }] });

    // Show typing indicator
    appendTyping();

    try {
        // Build the request with system instruction + history
        const requestBody = {
            system_instruction: {
                parts: [{ text: SYSTEM_PROMPT }]
            },
            contents: conversationHistory,
            generationConfig: {
                temperature: 0.7,
                maxOutputTokens: 512
            }
        };

        const response = await fetch(GEMINI_API_URL, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(requestBody)
        });

        const data = await response.json();

        if (!response.ok) {
            const errMsg = data?.error?.message || `HTTP ${response.status}`;
            console.error('Gemini API Error:', data);
            throw new Error(errMsg);
        }

        const botText = data?.candidates?.[0]?.content?.parts?.[0]?.text
            || 'Sorry, I couldn\'t understand that. Please try again.';

        // Update history
        conversationHistory.push({ role: 'model', parts: [{ text: botText }] });

        removeTyping();
        appendMessage('bot', formatBotText(botText), true);

    } catch (err) {
        removeTyping();
        console.error('SmartSpend AI Error:', err);

        // Show actual error to help diagnose
        const userMsg = err.message && err.message.length < 200
            ? `⚠️ Error: ${err.message}`
            : '⚠️ Could not reach the assistant. Please try again in a moment.';
        appendMessage('bot', formatBotText(userMsg), true);
    } finally {
        sendBtn.disabled = false;
        input.focus();
    }
}
