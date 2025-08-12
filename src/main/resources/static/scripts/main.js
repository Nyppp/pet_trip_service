// main.js
document.addEventListener('DOMContentLoaded', function() {
    const input = document.querySelector('#chat-input');
    const sendBtn = document.querySelector('#chat-modal-input button');
    const toggleBtn = document.querySelector('#chat-toggle-btn button');
    const modal = document.querySelector('#chat-modal');
    const chatBody = document.querySelector('#chat-modal-body');

    if (!input || !sendBtn || !toggleBtn || !modal || !chatBody) {
        console.warn('채팅요소를 찾을 수 없습니다. 셀렉터를 확인하세요.')
        return;
    }

    // 내 채팅방 id 확보
    let roomId = null;
    sendBtn.disabled = true;

    const initChatRoom = async () => {
      try {
        const res = await fetch('/chatrooms/me', { method: 'GET' });
        if (res.status === 401) {
          alert('채팅은 로그인 후 이용 가능합니다.');
          return;
        }
        const data = await res.json();
        roomId = data.roomId;
        sendBtn.disabled = false; // 초기화 완료 후 버튼 활성화
      } catch (e) {
        console.error('채팅방 초기화 실패:', e);
      }
    };


    const toggleChat = () => {
        modal.style.display = (modal.style.display === 'none') ? 'flex' : 'none';

    }

    // HTML 태그 이스케이프 처리 (XSS 방지)
    const escapeHTML = (str) =>
        String(str ?? '')
          .replace(/&/g, '&amp;')
          .replace(/</g, '&lt;');

   // 메세시 전송
   const sendMessage = async () => {
       const message = input.value;
       if (!message.trim()) return;
       if (!roomId) {
         alert('채팅방 정보를 불러오는 중입니다. 잠시 후 다시 시도해주세요.');
         return;
       }

       // 유저 메시지 UI
       const userMessage = document.createElement('div');
       userMessage.classList.add('chat-message', 'user-message');
       userMessage.innerHTML = escapeHTML(message).replace(/\n/g, '<br>');
       chatBody.appendChild(userMessage);
       chatBody.scrollTop = chatBody.scrollHeight;
       input.value = '';

       try {
         const res = await fetch(`/chatrooms/${roomId}/message`, {
           method: 'POST',
           headers: { 'Content-Type': 'application/json' },
           body: JSON.stringify({ message })
         });

         if (!res.ok) {
           if (res.status === 401) alert('로그인이 필요합니다.');
           throw new Error(`응답 오류: ${res.status}`);
         }

         const data = await res.json();
         const botReply = document.createElement('div');
         botReply.classList.add('chat-message', 'bot-message');
         botReply.innerHTML = escapeHTML(data.message).replace(/\n/g, '<br>');
         chatBody.appendChild(botReply);
         chatBody.scrollTop = chatBody.scrollHeight;

       } catch (error) {
         console.error('오류 발생:', error);
         const errMsg = document.createElement('div');
         errMsg.classList.add('chat-message', 'bot-message');
         errMsg.textContent = '죄송해요! 지금은 메시지를 보낼 수 없어요.';
         chatBody.appendChild(errMsg);
         chatBody.scrollTop = chatBody.scrollHeight;
       }
   };

    toggleBtn.addEventListener('click', toggleChat);
    sendBtn.addEventListener('click', sendMessage);
    input.addEventListener('keydown', function(event) {
        if (event.key === 'Enter') {
            event.preventDefault();
            sendMessage();
        }
        });

    // 초기 상태 보정
    if (!modal.style.display) modal.style.display = 'none';

    initChatRoom();
});