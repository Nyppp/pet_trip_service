document.addEventListener("DOMContentLoaded", () => {
  const startDateInput = document.getElementById("start_date");
  const endDateInput = document.getElementById("end_date");

  // 오늘 날짜로 시작일 제한
  const today = new Date().toISOString().split("T")[0];
  startDateInput.setAttribute("min", today);

  // 초기에는 종료일 비활성화
  endDateInput.disabled = true;

  startDateInput.addEventListener("change", () => {
    const selectedStartDate = startDateInput.value;

    if (selectedStartDate) {
      // 종료일 활성화 & 최소값 설정
      endDateInput.disabled = false;
      endDateInput.setAttribute("min", selectedStartDate);

      // 종료일이 시작일보다 이전이면 초기화
      if (endDateInput.value && endDateInput.value < selectedStartDate) {
        endDateInput.value = selectedStartDate;
      }
    } else {
      endDateInput.disabled = true;
      endDateInput.value = "";
    }
  });
});