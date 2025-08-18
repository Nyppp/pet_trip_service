document.addEventListener('DOMContentLoaded', function(){
    const carousel = document.querySelector('.places-carousel');
    if(!carousel) return;

    const track = carousel.querySelector('.carousel-track');
    const prev  = carousel.querySelector('.prev');
    const next  = carousel.querySelector('.next');

    const updateButtons = () => {
      const maxScroll = track.scrollWidth - track.clientWidth - 1; // 여유치
      prev.disabled = track.scrollLeft <= 0;
      next.disabled = track.scrollLeft >= maxScroll;
    };

    const scrollStep = () => track.clientWidth; // 한 화면씩 이동

    prev.addEventListener('click', () => {
      track.scrollBy({ left: -scrollStep(), behavior: 'smooth' });
    });
    next.addEventListener('click', () => {
      track.scrollBy({ left:  scrollStep(), behavior: 'smooth' });
    });

    track.addEventListener('scroll', updateButtons);
    window.addEventListener('resize', updateButtons);
    updateButtons();
  });