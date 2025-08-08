// main.js
document.addEventListener('DOMContentLoaded', function() {
    const input = document.querySelector('#chat-input');

    input.addEventListener('keydown', function(event) {
        if (event.key === 'Enter') {
            event.preventDefault();
            sendMessage();
        }
        });
});

function toggleChat() {
        const modal = document.getElementById("chat-modal");
        modal.style.display = modal.style.display === 'none' ? 'flex' : 'none';
}

function sendMessage() {
    const input = document.getElementById('chat-input');
    const message = input.value;

    const chatBody = document.getElementById('chat-modal-body');
    const userMessage = document.createElement('div');
    userMessage.classList.add('chat-message', 'user-message');
    userMessage.textContent = input.value;
    chatBody.appendChild(userMessage);

    console.log(message);
    if (!message.trim()) return;

    fetch('/chatrooms/1/message', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({message})
    })
    .then(response => response.json())
    .then(data => {
        const chatBody = document.getElementById('chat-modal-body');
        const botReply = document.createElement('div');
        botReply.classList.add('chat-message', 'bot-message');
        console.log(data.message);
        botReply.textContent = data.message;

        chatBody.appendChild(botReply);
        input.value = '';
    })
    .catch(error => {
        console.error('오류 발생:', error);
    });
}