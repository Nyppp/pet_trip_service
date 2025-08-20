document.addEventListener("DOMContentLoaded", () => {
  const pathSegments = window.location.pathname.split("/");
  const userId = pathSegments[2];
  const scheduleId = pathSegments[4];

  fetch(`/api/schedules/${scheduleId}/items`)
    .then((res) => res.json())
    .then((data) => {
      const container = document.getElementById("planner-body");

      // 데이터가 없을 때 안내 메시지 표시
      if (!data || data.length === 0) {
        const emptyMessage = document.createElement("div");
        emptyMessage.className = "empty-message";
        emptyMessage.innerHTML = `
          <div style="text-align: center; padding: 3rem 1rem;">
            <div style="font-size: 1.2rem; color: #666; margin-bottom: 1rem;">
              아직 추가된 장소가 없습니다
            </div>
            <div style="color: #999;">
              여행 일정에 장소를 추가해보세요!
            </div>
          </div>
        `;
        container.appendChild(emptyMessage);
        return;
      }

      data
        .sort((a, b) => new Date(a.startTime) - new Date(b.startTime))
        .forEach((item) => {
          const card = document.createElement("div");
          card.className = "location-card";
          card.dataset.date = item.startTime.substring(0, 10);

          // 이미지
          const img = document.createElement("img");
          img.className = "location-thumb";
          img.alt = "장소 이미지";
          img.src = item.placeImgUrl && /^https?:\/\//.test(item.placeImgUrl) ? item.placeImgUrl : "/images/default.png";

          // 본문
          const body = document.createElement("div");
          body.className = "location-body";

          // 제목
          const title = document.createElement("h3");
          title.className = "location-title";
          title.textContent = item.placeName ?? "";

          // 날짜
          const dateP = document.createElement("p");
          dateP.className = "location-date";
          const dateLabel = document.createElement("span");
          dateLabel.className = "label";
          dateLabel.textContent = "날짜";
          const dateText = document.createElement("span");
          const date = item.startTime.substring(0, 10);
          dateText.textContent = date;
          dateP.append(dateLabel, document.createTextNode(" "), dateText);

          // 시간
          const timeOnlyP = document.createElement("p");
          timeOnlyP.className = "location-time-only";
          const timeOnlyLabel = document.createElement("span");
          timeOnlyLabel.className = "label";
          timeOnlyLabel.textContent = "시간";
          const timeOnlyText = document.createElement("span");

          // 시간 포맷 함수
          const formatTime = (timeString) => {
            const time = timeString.substring(11, 16); // HH:MM 형식 추출
            const [hours, minutes] = time.split(":");
            const hour = parseInt(hours);
            const ampm = hour >= 12 ? "오후" : "오전";
            const displayHour = hour === 0 ? 12 : hour > 12 ? hour - 12 : hour;
            return `${ampm} ${displayHour.toString().padStart(2, "0")}:${minutes}`;
          };

          const startTime = formatTime(item.startTime);
          const endTime = formatTime(item.endTime);
          timeOnlyText.textContent = `${startTime} ~ ${endTime}`;
          timeOnlyP.append(timeOnlyLabel, document.createTextNode(" "), timeOnlyText);

          // 메모
          const memoP = document.createElement("p");
          memoP.className = "location-memo";
          const memoLabel = document.createElement("span");
          memoLabel.className = "label";
          memoLabel.textContent = "메모";
          const memoText = document.createElement("span");
          memoText.textContent = item.memo ?? "";
          memoText.style.whiteSpace = "pre-line";
          memoP.append(memoLabel, document.createTextNode(" "), memoText);

          // 카드 클릭 이벤트 추가
          card.addEventListener("click", () => {
            window.location.href = `/users/${userId}/schedules/${scheduleId}/items/${item.id}`;
          });

          // 조립
          body.append(title, dateP, timeOnlyP, memoP);
          card.append(img, body);
          container.appendChild(card);
        });

      /* ====== 드롭다운 채우기 ====== */
      const dateSelect = document.getElementById("date_filter");
      if (dateSelect) {
        // 모든 카드에서 날짜 추출 → Set으로 중복 제거 → 정렬
        const allDates = [...new Set(data.map((item) => item.startTime.split("T")[0]))].sort();

        // 전체 옵션
        const allOption = document.createElement("option");
        allOption.value = "all";
        allOption.textContent = "전체";
        dateSelect.appendChild(allOption);

        // 날짜 옵션
        allDates.forEach((date) => {
          const opt = document.createElement("option");
          opt.value = date;
          opt.textContent = date;
          dateSelect.appendChild(opt);
        });

        // 필터 이벤트
        dateSelect.addEventListener("change", () => {
          const selected = dateSelect.value;
          document.querySelectorAll(".location-card").forEach((el) => {
            if (selected === "all") {
              el.style.display = "";
            } else {
              el.style.display = el.dataset.date === selected ? "" : "none";
            }
          });
        });
      }
    })
    .catch((err) => console.error("불러오기 실패:", err));
});
