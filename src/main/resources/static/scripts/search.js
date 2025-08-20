(() => {
    const $  = (sel, el = document) => el.querySelector(sel);
    const $$ = (sel, el = document) => Array.from(el.querySelectorAll(sel));

    function initSortRadios() {
        const sortHidden = $('#sort');
        const sortRadios = $$('input[name="sort-radio"]');
        if (!sortRadios.length) return;

        sortRadios.forEach(r => {
            r.addEventListener('change', () => {
                if (!r.checked) return;
                if (sortHidden) sortHidden.value = r.value;
                const pageHidden = $('input[name="page"]');
                if (pageHidden) pageHidden.value = 0;  // 정렬 바꾸면 1페이지로
                $('#search-form')?.submit();
            });
        });
    }

    function initResetButton() {
        const resetBtn = $('#cat-reset');
        if (!resetBtn) return;
        resetBtn.addEventListener('click', () => {
            const cat = $('#cat1');
            if (cat) cat.value = '';
            const pageHidden = $('input[name="page"]');
            if (pageHidden) pageHidden.value = 0;
            $('#search-form')?.submit();
        });
    }

    function initPagination() {
        const pager = $('#pagination');
        if (!pager) return;

        const page    = Number(pager.dataset.page || 0);
        const size    = Number(pager.dataset.size || 12);
        const total   = Number(pager.dataset.total || 0);
        const cat1    = pager.dataset.cat1 || '';
        const keyword = pager.dataset.keyword || '';
        const sort    = pager.dataset.sort || $('#sort')?.value || 'relevance';

        const totalPages = total > 0 ? Math.floor((total - 1) / size) + 1 : 0;
        if (totalPages <= 1) {
            pager.innerHTML = '';
            return;
        }

        const mkHref = (p) => {
            const params = new URLSearchParams();
            if (cat1) params.set('cat1', cat1);
            if (keyword) params.set('keyword', keyword);
            params.set('page', p);
            params.set('size', size);
            params.set('sort', sort);     // ✅ 정렬 유지
            return `/search?${params.toString()}`;
        };

        const mkLink = (p, label, disabled = false, active = false) =>
            `<a class="page-link ${disabled ? 'disabled' : ''} ${active ? 'active' : ''}"
          href="${disabled ? 'javascript:void(0)' : mkHref(p)}">${label}</a>`;

        const prev = mkLink(Math.max(0, page - 1), '이전', page === 0);
        const next = mkLink(Math.min(totalPages - 1, page + 1), '다음', page >= totalPages - 1);

        const windowSize = 5;
        const start = Math.max(0, Math.min(page - Math.floor(windowSize / 2), Math.max(0, totalPages - windowSize)));
        const end   = Math.min(totalPages - 1, start + windowSize - 1);

        let nums = '';
        for (let p = start; p <= end; p++) nums += mkLink(p, (p + 1), false, p === page);

        pager.innerHTML = `<div class="pager">${prev}${nums}${next}</div>`;
    }

    function initLikeButtons() {
        const ICON_BASE    = '/images/';
        const ICON_FILLED  = 'heart_filled.svg';
        const ICON_OUTLINE = 'heart_outline.svg';

        const CSRF = (() => {
            const token  = document.querySelector('meta[name="_csrf"]')?.getAttribute('content');
            const header = document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content');
            return token && header ? { header, token } : null;
        })();

        const setUI = (btn, liked, count) => {
            const img = btn.querySelector('.like-img');
            const cnt = btn.closest('.like-box')?.querySelector('.like-count');
            if (img) img.src = ICON_BASE + (liked ? ICON_FILLED : ICON_OUTLINE);
            btn.classList.toggle('liked', !!liked);
            btn.setAttribute('aria-pressed', String(!!liked));
            if (typeof count === 'number' && cnt) cnt.textContent = count;
        };

        $$('.result-card .like-icon').forEach(btn => {
            setUI(btn, btn.classList.contains('liked'));

            btn.addEventListener('click', async (e) => {
                e.preventDefault();
                e.stopPropagation(); // ✅ 카드 클릭 네비게이션과 분리
                const placeId = btn.dataset.placeId;
                if (!placeId) return;

                const likedNow = btn.classList.contains('liked');
                const method   = likedNow ? 'DELETE' : 'POST';

                try {
                    const headers = { 'X-Requested-With': 'XMLHttpRequest' };
                    if (CSRF) headers[CSRF.header] = CSRF.token;

                    const res = await fetch(`/api/places/${placeId}/like`, { method, headers });
                    if (res.status === 401) {
                        location.href = '/login';
                        return;
                    }
                    if (!res.ok) throw new Error('like api error');

                    const data = await res.json(); // { liked: boolean, count: number }
                    setUI(btn, data.liked === true, data.count);
                } catch (err) {
                    console.error(err);
                    alert('잠시 후 다시 시도해 주세요.');
                }
            });
        });
    }

    function initCardNavigation() {
        document.addEventListener('click', (e) => {
            const card = e.target.closest('.js-card');
            if (!card) return;

            if (e.target.closest('.like-box') || e.target.closest('a') || e.target.tagName === 'BUTTON') return;

            const href = card.dataset.href;
            if (href) window.location.href = href;
        });

        document.addEventListener('keydown', (e) => {
            if (!(e.key === 'Enter' || e.key === ' ')) return;
            const card = e.target.closest('.js-card');
            if (!card) return;
            e.preventDefault(); // 스페이스 스크롤 방지
            const href = card.dataset.href;
            if (href) window.location.href = href;
        });
    }

    initSortRadios();
    initResetButton();
    initPagination();
    initLikeButtons();
    initCardNavigation();
})();
