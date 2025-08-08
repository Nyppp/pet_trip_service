document.addEventListener("DOMContentLoaded", () => {
    const pathSegments = window.location.pathname.split('/');
    const scheduleId = pathSegments[2]; // "1"이 됨

    fetch(`/api/schedules/${scheduleId}/scheduleItems`)
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
                const dateHeader = document.createElement("div");
                dateHeader.textContent = date;
                container.appendChild(dateHeader);

                items
                .sort((a, b) => new Date(a.startTime) - new Date(b.startTime))
                .forEach(item =>{
                    const itemDiv = document.createElement("div");
                    itemDiv.innerHTML = `
                    <img src="${item.placeImgUrl}"></img>
                    <h3>${item.placeName}</h3>
                    <p>일정 시각 : ${item.startTime.substring(11,16)} ~ ${item.endTime.substring(11,16)}</p>
                    <p>메모 : ${item.memo}</p>

                    <a href="/schedule/${scheduleId}/items/${item.id}">일정 상세보기</a>
                    `

                    itemDiv.classList.add("location-card")
                    container.appendChild(itemDiv);
                });

            });
        })
    .catch(err => console.error("불러오기 실패:", err));
});