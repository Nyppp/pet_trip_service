(() => {
  const form = document.getElementById('schedule_form');
  if (!form) return;

  const userId     = form.dataset.userId;
  const scheduleId = form.dataset.scheduleId; // 생성이면 빈값/undefined
  const isNew      = form.dataset.isNew === 'true';

  const csrfToken  = document.querySelector('meta[name="_csrf"]')?.content;
  const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.content;

  form.addEventListener('submit', async (e) => {
    e.preventDefault();

    // th:field로 바인딩된 name으로 FormData 생성
    const fd = new FormData(form);
    const payload = Object.fromEntries(fd.entries());
    // 필요 시 타입 보정 (예: 날짜 포맷/문자열 트림 등)

    const url = isNew
      ? `/api/users/${userId}/schedules`                                  // 생성
      : `/api/users/${userId}/schedules/${encodeURIComponent(scheduleId)}`; // 수정

    const method = isNew ? 'POST' : 'PATCH';

    const headers = { 'Content-Type': 'application/json' };
    if (csrfToken && csrfHeader) headers[csrfHeader] = csrfToken;

    try {
      const res = await fetch(url, {
        method,
        headers,
        body: JSON.stringify(payload)
      });

      if (res.status === 201) {
        const placeId = new URLSearchParams(window.location.search).get("placeId");


        const url = new URL(`/users/${userId}/schedules`, window.location.origin);
        if (placeId) url.searchParams.set("placeId", placeId);

        window.location.href = url.toString();
        return;
      }

      if (res.status === 204) {
        // 바디 없는 성공
        window.location.href = `/users/${userId}/schedules`;
        return;
      }

      // 그 외 상태: 메시지 파싱 시도
      let msg;
      const text = await res.text();
      try { msg = JSON.parse(text); } catch { msg = text; }
      alert(`요청 실패 (${res.status}): ${typeof msg === 'string' ? msg : (msg.message || 'Unknown error')}`);

    } catch (err) {
      console.error(err);
      alert('네트워크 오류가 발생했습니다.');
    }
  });
})();
