document.addEventListener("DOMContentLoaded", () => {
  const pathParts = window.location.pathname.split("/");
  const userId = pathParts[2];

  const params = new URLSearchParams(window.location.search);
  const placeId = params.get("placeId");
  const isSelectMode = !!placeId;

  fetch(`/api/users/${userId}/schedules`)
    .then((res) => res.json())
    .then((data) => {
      const scheduleList = document.getElementById("schedule_list");
      const emptyMessage = document.getElementById("empty_message");

      if (data.length === 0) {
        emptyMessage.style.display = "block";
      } else {
        emptyMessage.style.display = "none";
      }

      data.forEach((schedule) => {
        const cardDiv = document.createElement("div");
        cardDiv.className = "schedule_card";
        cardDiv.style.cursor = "pointer";

        // 카드 클릭 이벤트 (스케줄 상세 페이지로 이동)
        cardDiv.addEventListener("click", (e) => {
          // 메뉴 버튼이나 드롭다운 메뉴 클릭 시에는 카드 클릭 무시
          if (e.target.closest(".menu_button") || e.target.closest(".dropdown_menu")) {
            return;
          }
          window.location.href = `/users/${userId}/schedules/${schedule.id}`;
        });

        // 헤더 영역 (제목 + 메뉴 버튼)
        const headerDiv = document.createElement("div");
        headerDiv.className = "schedule_header";

        const titleLink = document.createElement("span");
        titleLink.className = "schedule_title";
        titleLink.textContent = schedule.title;

        const menuButton = document.createElement("button");
        menuButton.className = "menu_button";
        menuButton.innerHTML = "⋮";
        menuButton.setAttribute("aria-label", "메뉴");

        // 드롭다운 메뉴
        const dropdownMenu = document.createElement("div");
        dropdownMenu.className = "dropdown_menu";

        const updateItem = document.createElement("button");
        updateItem.className = "dropdown_item update";
        updateItem.textContent = "수정";
        updateItem.onclick = () => {
          window.location.href = `/users/${userId}/schedules/${schedule.id}/edit`;
        };

        const deleteItem = document.createElement("button");
        deleteItem.className = "dropdown_item delete";
        deleteItem.textContent = "삭제";
        deleteItem.onclick = () => {
          if (!confirm(`"${schedule.title}" 일정을 삭제할까요?`)) return;

          deleteItem.disabled = true;
          deleteItem.textContent = "삭제 중...";

          fetch(`/api/schedules/${schedule.id}`, {
            method: "DELETE",
          })
            .then((res) => {
              if (res.status === 204) {
                cardDiv.style.transition = "all 0.3s ease";
                cardDiv.style.opacity = "0";
                cardDiv.style.transform = "translateX(-100px)";

                setTimeout(() => {
                  scheduleList.removeChild(cardDiv);
                  if (scheduleList.children.length === 0) {
                    emptyMessage.style.display = "block";
                  }
                }, 300);
              } else {
                alert("삭제 실패");
                deleteItem.disabled = false;
                deleteItem.textContent = "삭제";
              }
            })
            .catch((error) => {
              console.error("삭제 오류:", error);
              alert("삭제 중 오류가 발생했습니다.");
              deleteItem.disabled = false;
              deleteItem.textContent = "삭제";
            });
        };

        // 메뉴 버튼 클릭 이벤트
        menuButton.onclick = (e) => {
          e.stopPropagation();
          dropdownMenu.classList.toggle("show");
        };

        // 다른 곳 클릭 시 메뉴 닫기
        document.addEventListener("click", (e) => {
          if (!cardDiv.contains(e.target)) {
            dropdownMenu.classList.remove("show");
          }
        });

        const dateDiv = document.createElement("div");
        dateDiv.className = "schedule_date";

        const startDate = new Date(schedule.startDate);
        const endDate = new Date(schedule.endDate);
        const formatDate = (date) => `${date.getFullYear()}.${(date.getMonth() + 1).toString().padStart(2, "0")}.${date.getDate().toString().padStart(2, "0")}`;

        // 일정 일수 계산
        const timeDiff = endDate.getTime() - startDate.getTime();
        const daysDiff = Math.ceil(timeDiff / (1000 * 3600 * 24)) + 1;

        // 정보 섹션 생성
        const infoDiv = document.createElement("div");
        infoDiv.className = "schedule_info";

        // 날짜 정보 행
        const dateRow = document.createElement("div");
        dateRow.className = "info_row date";
        dateRow.innerHTML = `<span class="info_label">기간:</span><span class="info_value">${formatDate(startDate)} ~ ${formatDate(endDate)}</span>`;

        // 일정 일수 정보 행
        const durationRow = document.createElement("div");
        durationRow.className = "info_row duration";
        durationRow.innerHTML = `<span class="info_label">일정:</span><span class="info_value">총 ${daysDiff}일</span>`;

        infoDiv.appendChild(dateRow);
        infoDiv.appendChild(durationRow);

        // 선택 모드일 때만 기존 버튼들 표시
        if (isSelectMode) {
          const actionDiv = document.createElement("div");
          actionDiv.className = "schedule_action";

          const selectButton = document.createElement("a");
          selectButton.textContent = "여기 추가";
          selectButton.className = "select_button";
          selectButton.href = `/users/${userId}/schedules/${schedule.id}/items/new?placeId=${encodeURIComponent(placeId)}`;
          actionDiv.appendChild(selectButton);

          // 헤더 구성
          headerDiv.appendChild(titleLink);
          headerDiv.appendChild(menuButton);
          dropdownMenu.appendChild(updateItem);
          dropdownMenu.appendChild(deleteItem);

          cardDiv.appendChild(headerDiv);
          cardDiv.appendChild(dropdownMenu);
          cardDiv.appendChild(infoDiv);
          cardDiv.appendChild(actionDiv);
        } else {
          // 헤더 구성
          headerDiv.appendChild(titleLink);
          headerDiv.appendChild(menuButton);
          dropdownMenu.appendChild(updateItem);
          dropdownMenu.appendChild(deleteItem);

          cardDiv.appendChild(headerDiv);
          cardDiv.appendChild(dropdownMenu);
          cardDiv.appendChild(infoDiv);
        }

        scheduleList.appendChild(cardDiv);
      });
    });
});
