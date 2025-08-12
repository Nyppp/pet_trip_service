(function () {
  // 1) URL에서 itemId 추출
  const segs = window.location.pathname.split('/').filter(Boolean);
  const itemsIdx = segs.indexOf('items');
  if (itemsIdx === -1 || !segs[itemsIdx + 1]) {
    alert('잘못된 경로입니다. (itemId 없음)');
    return;
  }

  const pathParts = window.location.pathname.split("/");
  const userId = pathParts[2];
  const scheduleId = pathParts[4];


  const itemId = segs[itemsIdx + 1];

  // 2) API 엔드포인트
  const apiUrl = `/api/items/${itemId}`;

  // 3) DOM 요소 캐시
  const $img = document.getElementById('item_main_img');
  const $title = document.getElementById('item_title');
  const $start = document.getElementById('start_time');
  const $end = document.getElementById('end_time');
  const $memo = document.getElementById('memo');
  const $edit = document.getElementById('edit_link');
  const $list = document.getElementById('list_link');
  const $del = document.getElementById('delete_btn');

  // 4) 날짜 포맷
  function fmt(dtString) {
    if (!dtString) return '-';
    const d = new Date(dtString);
    if (isNaN(d)) return dtString;
    return new Intl.DateTimeFormat('ko-KR', {
      year: 'numeric', month: '2-digit', day: '2-digit',
      hour: '2-digit', minute: '2-digit'
    }).format(d).replace(/\./g,'-').replace(/\s/g,' ').trim();
  }

  // 5) 상세 조회
  async function loadDetail() {
    try {
      const res = await fetch(`/api/items/${itemId}`, { credentials: 'include' });
      if (!res.ok) throw new Error(`조회 실패: ${res.status}`);
      const data = await res.json();

      $title.textContent = data?.placeName ?? '(이름 없음)';
      $start.textContent = fmt(data?.startTime);
      $end.textContent = fmt(data?.endTime);
      $memo.textContent = data?.memo ?? '-';

      const imgUrl = data?.placeImgUrl ??  '';
      if (imgUrl) {
        $img.src = imgUrl;
        $img.alt = data?.placeName ?? '장소 이미지';
      }

      // 수정 / 리스트 URL 구성
      $edit.href = `/items/${itemId}/edit`;
      $list.href = `/users/${userId}/schedules/${data.scheduleId}`;
    } catch (e) {
      console.error(e);
      alert('상세 정보를 불러오지 못했습니다.');
    }
  }

  // 6) 삭제
  async function deleteItem() {
    if (!confirm('정말 삭제할까요?')) return;
    try {
      const res = await fetch(`/api/schedules/${scheduleId}/items/${itemId}`, { method: 'DELETE', credentials: 'include' });
      if (res.status === 204) {
        // DTO에 scheduleId가 있으니 리스트 페이지로 이동
        window.location.href = $list.href;
      } else {
        throw new Error(`삭제 실패: ${res.status}`);
      }
    } catch (e) {
      console.error(e);
      alert('삭제 중 오류가 발생했습니다.');
    }
  }

  // 이벤트 등록
  $del.addEventListener('click', deleteItem);

  // 로드 시 데이터 불러오기
  loadDetail();
})();
