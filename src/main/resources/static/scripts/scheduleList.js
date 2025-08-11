document.addEventListener("DOMContentLoaded", () =>{

    const pathParts = window.location.pathname.split("/");
    const userId = pathParts[2];

    fetch(`/api/users/${userId}/schedules`)
    .then(res=>res.json())
    .then(data =>{
        const scheduleList = document.getElementById("schedule_list");
        const emptyMessage = document.getElementById("empty_message");

        if (data.length === 0) {
              emptyMessage.style.display = "block";
            } else {
              emptyMessage.style.display = "none";
            }

        data.forEach(schedule =>{
            const cardDiv = document.createElement("div");
            cardDiv.className = "schedule_card";

            const titleLink = document.createElement("a");
            titleLink.className = "schedule_title";
            titleLink.href = `/schedule/${schedule.id}`;
            titleLink.textContent = schedule.title;

            const dateDiv = document.createElement("div");
            dateDiv.className = "schedule_date";

            const actionDiv = document.createElement("div");
            actionDiv.className = "schedule_action";



            const startDate = new Date(schedule.startDate);
            const endDate = new Date(schedule.endDate);
            const formatDate = (date) =>
            `${date.getFullYear()}.${(date.getMonth() + 1)
                .toString()
                .padStart(2, "0")}.${date.getDate().toString().padStart(2, "0")}`;

            dateDiv.textContent = `기간 : ${formatDate(startDate)} ~ ${formatDate(endDate)}`;

            const deleteButton = document.createElement("button");
            deleteButton.textContent = "삭제";
            deleteButton.className = "delete_button";
            deleteButton.addEventListener("click", (e) => {
                e.preventDefault(); // 링크 이동 방지

                if (!confirm(`"${schedule.title}" 일정을 삭제할까요?`)) return;

                fetch(`/api/schedules/${schedule.id}`, {
                    method: "DELETE"
                })
                .then(res => {
                    if (res.status === 204) {
                        scheduleList.removeChild(cardDiv);
                        if (scheduleList.children.length === 0) {
                          emptyMessage.style.display = "block";
                        }
                    } else {
                        alert("삭제 실패");
                    }
                });
            });

            const updateButton = document.createElement("a"); //
            updateButton.textContent = "수정";
            updateButton.className = "update_button";
            updateButton.href = `/users/${userId}/schedules/${schedule.id}/edit`;

            cardDiv.appendChild(titleLink);
            cardDiv.appendChild(dateDiv);
            actionDiv.appendChild(updateButton);
            actionDiv.appendChild(deleteButton);
            cardDiv.appendChild(actionDiv);
            scheduleList.appendChild(cardDiv);
        });
    });

});