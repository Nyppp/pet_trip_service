function deleteScheduleItem(scheduleId, itemId) {
    if (!confirm("정말 삭제할까요?")) return;

    fetch(`/api/schedules/${scheduleId}/items/${itemId}`, {
        method: "DELETE"
    })
    .then(res => {
        if (res.status === 204) {
            alert("삭제 성공");
            location.href = `/schedule/${scheduleId}`;
        } else {
            alert("삭제 실패");
        }
    });
}