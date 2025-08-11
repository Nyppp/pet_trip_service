document.addEventListener("DOMContentLoaded", () => {
    const pathSegments = window.location.pathname.split('/');
    const scheduleId = pathSegments[2]; // "1"이 됨

    fetch(`/api/schedules/${scheduleId}/items`)
        .then(res => res.json())
        .then(data => {
            const groupedByDate = {};

            data.forEach(item => {
                const date = item.startTime.split("T")[0]; // yyyy-MM-dd 형태 추출

                if (!groupedByDate[date]) {
                    groupedByDate[date] = [];
                }
                groupedByDate[date].push(item);
            });

            const container = document.getElementById("planner-body")
            Object.entries(groupedByDate)
            .sort(([dateA], [dateB]) => new Date(dateA) - new Date(dateB))
            .forEach(([date, items])=>{

                items
                .sort((a, b) => new Date(a.startTime) - new Date(b.startTime))
                .forEach(item =>{
                    const dateKey = item.startTime.substring(0, 10);

                    const card = document.createElement("div");
                    card.className = "location-card";
                    card.dataset.date = dateKey;

                    // 이미지
                    const img = document.createElement("img");
                    img.className = "location-thumb";
                    img.alt = "장소 이미지";
                    img.src = (item.placeImgUrl && /^https?:\/\//.test(item.placeImgUrl))
                      ? item.placeImgUrl
                      : "/images/default.png";

                    // 본문
                    const body = document.createElement("div");
                    body.className = "location-body";

                    // 제목
                    const title = document.createElement("h3");
                    title.className = "location-title";
                    title.textContent = item.placeName ?? "";

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

                    // 일정
                    const timeP = document.createElement("p");
                    timeP.className = "location-time";
                    const timeLabel = document.createElement("span");
                    timeLabel.className = "label";
                    timeLabel.textContent = "일정";
                    const timeText = document.createElement("span");
                    const start = item.startTime.substring(5,16).replace("T"," ");
                    const end   = item.endTime.substring(5,16).replace("T"," ");
                    timeText.textContent = `${start} ~ ${end}`;
                    timeP.append(timeLabel, document.createTextNode(" "), timeText);

                    // 링크
                    const link = document.createElement("a");
                    link.className = "location-link";
                    link.href = `/schedule/${scheduleId}/items/${item.id}`;
                    link.textContent = "일정 상세보기";

                    // 조립
                    body.append(title, memoP, timeP, link);
                    card.append(img, body);
                    container.appendChild(card);
                });

            });
            /* ====== 여기서부터 드롭다운 채우기 ====== */
            const dateSelect = document.getElementById("date_filter");
            if (dateSelect) {
                // 모든 카드에서 날짜 추출 → Set으로 중복 제거 → 정렬
                const allDates = [...new Set(data.map(item => item.startTime.split("T")[0]))].sort();

                // 전체 옵션
                const allOption = document.createElement("option");
                allOption.value = "all";
                allOption.textContent = "전체";
                dateSelect.appendChild(allOption);

                // 날짜 옵션
                allDates.forEach(date => {
                    const opt = document.createElement("option");
                    opt.value = date;
                    opt.textContent = date;
                    dateSelect.appendChild(opt);
                });

                // 필터 이벤트
                dateSelect.addEventListener("change", () => {
                    const selected = dateSelect.value;
                    document.querySelectorAll(".location-card, .day-group").forEach(el => {
                        if (selected === "all") {
                            el.style.display = "";
                        } else {
                            if (el.classList.contains("location-card")) {
                                el.style.display = (el.dataset.date === selected) ? "" : "none";
                            } else if (el.classList.contains("day-group")) {
                                // 해당 날짜의 카드가 하나라도 보이면 header 표시
                                const anyVisible = Array.from(el.nextElementSibling ? [el.nextElementSibling] : [])
                                    .some(card => card.dataset.date === selected && card.style.display !== "none");
                                el.style.display = anyVisible ? "" : "none";
                            }
                        }
                    });
                });
            }
        })
    .catch(err => console.error("불러오기 실패:", err));
});