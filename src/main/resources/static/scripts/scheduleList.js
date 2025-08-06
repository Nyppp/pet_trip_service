document.addEventListener("DOMContentLoaded", () =>{
    fetch("/api/schedules")
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
            const link = document.createElement("a");
            link.href = `/schedule/${schedule.id}`; 
            link.className = "schedule_card";

            const titleDiv = document.createElement("div");
            titleDiv.className = "schedule_title";
            titleDiv.textContent = schedule.title;

            const dateDiv = document.createElement("div");
            dateDiv.className = "schedule_date";



            const startDate = new Date(schedule.startDate);
            const endDate = new Date(schedule.endDate);
            const formatDate = (date) =>
            `${date.getFullYear()}.${(date.getMonth() + 1)
                .toString()
                .padStart(2, "0")}.${date.getDate().toString().padStart(2, "0")}`;

            dateDiv.textContent = `${formatDate(startDate)} ~ ${formatDate(endDate)}`;

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
                        scheduleList.removeChild(link);
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
            updateButton.href = `/schedule/${schedule.id}/edit`;

            link.appendChild(titleDiv);
            link.appendChild(dateDiv);
            link.appendChild(deleteButton);
            link.appendChild(updateButton);
            scheduleList.appendChild(link);
        });
    });

});