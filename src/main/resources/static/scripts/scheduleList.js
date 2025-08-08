document.addEventListener("DOMContentLoaded", () =>{
    fetch("/api/schedules")
    .then(res=>res.json())
    .then(data =>{
        const scheduleList = document.getElementById("schedule_list");
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

            link.appendChild(titleDiv);
            link.appendChild(dateDiv);
            scheduleList.appendChild(link);
        });
    });

});