document.addEventListener('DOMContentLoaded', () => {
  const startTimeInput = document.getElementById('start_time');
  const endTimeInput = document.getElementById('end_time');
  const el = document.getElementById("schedule-data");

  // 상위 스케쥴 시작/종료 날짜 - 서버에서 미리 렌더링
  const scheduleStart = el.dataset.startDate;  // 타임리프 등으로 서버에서 삽입
  const scheduleEnd = el.dataset.endDate;

  // min/max 제한 설정
  const minDateTime = scheduleStart + "T00:00";
  const maxDateTime = scheduleEnd + "T23:59";

  startTimeInput.min = minDateTime;
  startTimeInput.max = maxDateTime;
  endTimeInput.min = minDateTime;
  endTimeInput.max = maxDateTime;

  // 시작일 선택 시, 종료일 자동 세팅
  startTimeInput.addEventListener('change', () => {
    const startDateTime = startTimeInput.value;

    if (startDateTime) {
      const startDate = startDateTime.split("T")[0]; // 날짜만
      const timePart = endTimeInput.value.split("T")[1] || "23:59";

      endTimeInput.value = startTimeInput.value; // 같은 날짜 유지
      endTimeInput.min = startTimeInput.value;
      endTimeInput.max = startDate + "T23:59";
    }
  });

  endTimeInput.addEventListener('change', () => {
      const startDateTime = startTimeInput.value;
      if (startTimeInput.value && endTimeInput.value < startTimeInput.value) {
              alert("종료 시간은 시작 시간보다 빠를 수 없습니다.");
              endTimeInput.value = startTimeInput.value;
            }
  });
});
