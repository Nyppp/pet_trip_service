// main.js
document.addEventListener('DOMContentLoaded', function () {
  const input = document.querySelector('#chat-input');
  const sendBtn = document.querySelector('#chat-modal-input button');
  const toggleBtn = document.querySelector('#chat-toggle-btn button');
  const modal = document.querySelector('#chat-modal');
  const chatBody = document.querySelector('#chat-modal-body');
  const clearBtn = document.querySelector('#clear-chat-btn');

  if (!input || !sendBtn || !toggleBtn || !modal || !chatBody || !clearBtn) {
    console.warn('채팅요소를 찾을 수 없습니다. 셀렉터를 확인하세요.');
    return;
  }

  // 로그인 필요 여부 플래그 (초기엔 미로그인 가정 X)
  let loginRequired = false;
  let loginUrl = '/login';

  // 채팅방 관련 변수
  let roomId = null;
  let initialized = false; // initChatRoom 중복 호출 방지
  sendBtn.disabled = true;
  let loadingOlder = false;   // 중복 로딩 방지
  const PAGE_SIZE = 5;
  let nextCursor = null;

  const initChatRoom = async () => {
    if (initialized) return;
    initialized = true;

    try {
      const res = await fetch('/chatrooms/me', { method: 'GET' });

      const ct = res.headers.get('content-type') || '';
      if (res.redirected || !ct.includes('application/json')) {
        loginRequired = true;
        return;
      }

      if (res.status === 401) {
        // 401 JSON이면 메시지/URL만 업데이트
        try {
          const data = await res.json();
          loginRequired = true;
          loginUrl = data?.loginUrl || '/login';
        } catch {
          loginRequired = true;
        }
        return;
      }

      // 정상
      const data = await res.json();
      roomId = data.roomId;
      sendBtn.disabled = false;

    } catch (e) {
      console.error('채팅방 초기화 실패:', e);
    }
  };

  const toggleChat = async () => {
    const opening = modal.style.display === 'none' || !modal.style.display;
    modal.style.display = opening ? 'flex' : 'none';

    if (opening && !initialized) {
      await initChatRoom();

      if (loginRequired) {
        alert('채팅은 로그인 후 이용 가능합니다.');
        window.location.href = loginUrl;
        return;
      }
      await loadLatest();
    }
  };

  const escapeHTML = s => String(s ?? '').replace(/&/g,'&amp;').replace(/</g,'&lt;');

  function addMessageBubble(dto, opt = {}) {
    const div = document.createElement('div');
    const isBot = dto.sender === 'chatbot';
    div.classList.add('chat-message', isBot ? 'bot-message' : 'user-message');
    div.innerHTML = escapeHTML(dto.message).replace(/\n/g, '<br>');

    if (opt.prepend) chatBody.prepend(div);
    else chatBody.appendChild(div);
  }

  const loadLatest = async () => {
    const res = await fetch(`/chatrooms/${roomId}/messages?size=${PAGE_SIZE}`);
    const data = await res.json();

    [...(data.items || [])].reverse()
           .forEach(msg => addMessageBubble(msg, { prepend: true }));

    chatBody.scrollTop = chatBody.scrollHeight;

    nextCursor = data.nextCursor;
  };

  chatBody.addEventListener('scroll', async () => {
    if (chatBody.scrollTop === 0) {
      await loadOlder();
    }
  });

  const loadOlder = async () => {
    if (!nextCursor || loadingOlder) return;
    loadingOlder = true;

    // 현재 맨 위의 높이를 저장해 두면 스크롤 점프 방지 가능
    const prevHeight = chatBody.scrollHeight;

    const res = await fetch(`/chatrooms/${roomId}/messages?cursor=${nextCursor}&size=${PAGE_SIZE}`);
    const data = await res.json();

    // 기존 메시지 위에 prepend
    [...(data.items || [])].reverse()
       .forEach(msg => addMessageBubble(msg, { prepend: true }));

    // 스크롤 위치 보정 (위에 붙였으니, 이전 위치 유지)
    const added = chatBody.scrollHeight - prevHeight;
    chatBody.scrollTop = added;

    nextCursor = data.nextCursor;
    loadingOlder = false;
  };

  const sendMessage = async () => {
    // 전송 시점에 로그인 필요하면 안내 + 이동
    if (loginRequired) {
      alert('로그인이 필요합니다.');
      window.location.href = loginUrl;
      return;
    }

    // 아직 초기화 안 했으면 우선 초기화
    if (!initialized) {
      await initChatRoom();
      if (loginRequired) {
        alert('로그인이 필요합니다.');
        window.location.href = loginUrl;
        return;
      }
    }

    const message = input.value;
    if (!message.trim()) return;

    if (!roomId) {
      alert('채팅방 정보를 불러오는 중입니다. 잠시 후 다시 시도해주세요.');
      return;
    }

    // 사용자 메시지 표시
    addMessageBubble({ sender: 'user', message }, {});

    chatBody.scrollTop = chatBody.scrollHeight;
    input.value = '';

    try {
      const res = await fetch(`/chatrooms/${roomId}/message`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ message })
      });

      const ct = res.headers.get('content-type') || '';
      if (res.redirected || !ct.includes('application/json')) {
        alert('로그인이 필요합니다.');
        window.location.href = loginUrl;
        return;
      }

      if (res.status === 401) {
        let msg = '로그인이 필요합니다.';
        try {
          const data = await res.json();
          msg = data?.error || msg;
          loginUrl = data?.loginUrl || '/login';
        } catch {}
        alert(msg);
        window.location.href = loginUrl;
        return;
      }

      if (!res.ok) throw new Error(`응답 오류: ${res.status}`);

      const data = await res.json();

      // 챗봇 메세지 추가
      addMessageBubble({ sender: 'chatbot', message: data.message}, {});

      chatBody.scrollTop = chatBody.scrollHeight;

    } catch (err) {
      console.error('오류 발생:', err);
      const errMsg = document.createElement('div');
      errMsg.classList.add('chat-message', 'bot-message');
      errMsg.textContent = '죄송해요! 지금은 메시지를 보낼 수 없어요.';
      chatBody.appendChild(errMsg);
      chatBody.scrollTop = chatBody.scrollHeight;
    }
  };

  // 이벤트
  toggleBtn.addEventListener('click', toggleChat);
  sendBtn.addEventListener('click', sendMessage);
  input.addEventListener('keydown', function (event) {
    if (event.key === 'Enter') {
      event.preventDefault();
      sendMessage();
    }
  });

  // 초기화 버튼
  if (clearBtn) {
      clearBtn.addEventListener("click", async () => {
        const confirmed = confirm("채팅방에 저장된 메세지를 모두 삭제합니다. 정말로 삭제하시겠습니까?");
        if (confirmed) {
          // 서버에 삭제 요청
          await fetch(`/chatrooms/${roomId}/messages`, {
            method: "DELETE"
          });

          // 프론트 화면 비우기
          chatBody.innerHTML = "";
        }
      });
  }
  // 초기 상태
  if (!modal.style.display) modal.style.display = 'none';
});
