// script.js (module)
'use strict';

/*
  Full frontend logic:
  - API wrapper with timeout and graceful fallback to local simulation (localStorage)
  - Single state object + setState/getState
  - Modular render functions for pages: customers, bookings, new booking, packages
  - Forms, inline editing via modal, CSV export, toast, overlay when API down
  - When creating customer -> automatically select them in booking forms and switch to New Booking page
*/

/* ----------------------------- Configuration ----------------------------- */
const CONFIG = {
    API_BASE: 'http://localhost:8080/api',
    FETCH_TIMEOUT_MS: 10000,
    FALLBACK_TO_LOCAL: true
};

/* ----------------------------- Utilities ----------------------------- */
const Util = {
    nowISO() { return new Date().toISOString(); },
    todayDate() {
        const d = new Date();
        d.setHours(0,0,0,0);
        return d.toISOString().slice(0,10);
    },
    formatCurrency(n){ return '$' + Number(n || 0).toFixed(2); },
    debounce(fn, wait=250){ let t; return (...a)=>{ clearTimeout(t); t=setTimeout(()=>fn(...a), wait); }; },
    download(filename, text){
        const blob = new Blob([text], {type: 'text/csv;charset=utf-8;'});
        const url = URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url; a.download = filename; document.body.appendChild(a); a.click(); a.remove();
        URL.revokeObjectURL(url);
    },
    escape(s){ if (s===undefined || s===null) return ''; return String(s).replace(/[&<>"']/g, m => ({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[m])); }
};

/* ----------------------------- Local DB Simulation ----------------------------- */
/* Uses key prefix "tbs_" and stores customers/bookings/packages matching DB shapes */
const LocalDB = (function(){
    const PREFIX = 'tbs_';
    // Initial seed from SQL (names aligned to API fields)
    const SEED = {
        customers: [
            { id:1, name:'Bob Chen', email:'bob.chen@email.com', phone:'+86-555-2002', passportNumber:'CN777888999', createdAt:'2026-02-09T00:00:00Z' },
            { id:2, name:'Updated Name', email:'updated.email@test.com', phone:'+1-555-9999', passportNumber:'US555888999', createdAt:'2026-02-09T00:00:00Z' },
            { id:3, name:'Alice Williams', email:'alice.w@email.com', phone:'+1-555-1001', passportNumber:'US555888999', createdAt:'2026-02-09T00:00:00Z' },
            { id:4, name:'Emma Johnson', email:'emma.j@email.com', phone:'+1-555-0102', passportNumber:'US987654321', createdAt:'2026-02-09T00:00:00Z' },
            { id:5, name:'Ahmed Hassan', email:'ahmed.h@email.com', phone:'+971-555-0103', passportNumber:'AE555666777', createdAt:'2026-02-09T00:00:00Z' }
        ],
        bookings: [
            { id:1, customerId:1, bookingDate:'2026-02-22', totalPrice:750.00, status:'PENDING', type:'FLIGHT', createdAt:'2026-02-01T00:00:00Z', flightNumber:'AA123', origin:'New York', destination:'Tokyo', seatClass:'BUSINESS' },
            { id:2, customerId:3, bookingDate:'2026-02-23', totalPrice:750.00, status:'PENDING', type:'FLIGHT', createdAt:'2026-02-02T00:00:00Z', flightNumber:'AA123', origin:'New York', destination:'Tokyo', seatClass:'BUSINESS' },
            { id:3, customerId:4, bookingDate:'2026-03-15', totalPrice:450.00, status:'CONFIRMED', type:'FLIGHT', createdAt:'2026-02-05T00:00:00Z', flightNumber:'EK205', origin:'Los Angeles', destination:'Dubai', seatClass:'ECONOMY' },
            { id:4, customerId:5, bookingDate:'2026-04-20', totalPrice:850.00, status:'PENDING', type:'FLIGHT', createdAt:'2026-02-10T00:00:00Z', flightNumber:'BA401', origin:'New York JFK', destination:'London Heathrow', seatClass:'BUSINESS' },
            { id:5, customerId:1, bookingDate:'2026-02-23', totalPrice:800.00, status:'PENDING', type:'HOTEL', createdAt:'2026-02-03T00:00:00Z', hotelName:'Grand Hyatt Tokyo', roomType:'DELUXE', nights:4 },
            { id:6, customerId:3, bookingDate:'2026-02-24', totalPrice:800.00, status:'PENDING', type:'HOTEL', createdAt:'2026-02-04T00:00:00Z', hotelName:'Grand Hyatt Tokyo', roomType:'DELUXE', nights:4 },
            { id:7, customerId:2, bookingDate:'2026-04-10', totalPrice:1200.00, status:'CONFIRMED', type:'HOTEL', createdAt:'2026-02-07T00:00:00Z', hotelName:'Le Grand Paris Hotel', roomType:'SUITE', nights:3 },
            { id:8, customerId:4, bookingDate:'2026-03-16', totalPrice:400.00, status:'CONFIRMED', type:'HOTEL', createdAt:'2026-02-08T00:00:00Z', hotelName:'London City Inn', roomType:'STANDARD', nights:4 }
        ],
        packages: [
            { id:1, name:'Tokyo Adventure Package', customerId:1, discount:10.00, bookingIds:[1,5] },
            { id:2, name:'European Tour', customerId:2, discount:15.00, bookingIds:[7] }
        ]
    };

    function _key(k){ return PREFIX + k; }
    function read(k){ try { return JSON.parse(localStorage.getItem(_key(k)) || 'null') || []; } catch(e){ return []; } }
    function write(k, v){ localStorage.setItem(_key(k), JSON.stringify(v)); }
    function ensureSeed(){
        if (!localStorage.getItem(_key('customers'))){
            write('customers', SEED.customers.slice());
            write('bookings', SEED.bookings.slice());
            write('packages', SEED.packages.slice());
        }
    }

    return {
        init(){ ensureSeed(); },
        getCustomers(){ return read('customers'); },
        getCustomer(id){ return read('customers').find(c => c.id === Number(id)) || null; },
        createCustomer(payload){
            const list = read('customers');
            const newId = list.length ? Math.max(...list.map(x=>x.id)) + 1 : 1;
            const obj = {
                id: newId,
                name: payload.name,
                email: payload.email,
                phone: payload.phone,
                passportNumber: payload.passportNumber,
                createdAt: Util.nowISO()
            };
            list.push(obj); write('customers', list);
            return obj;
        },
        updateCustomer(id, payload){
            const list = read('customers');
            const idx = list.findIndex(c => c.id === Number(id));
            if (idx === -1) throw new Error('Not found');
            list[idx] = { ...list[idx], ...payload };
            write('customers', list);
            return list[idx];
        },
        deleteCustomer(id){
            // block delete if bookings exist
            const bookings = read('bookings');
            if (bookings.some(b => b.customerId === Number(id))) {
                const err = new Error('Customer has related bookings');
                err.code = 'HAS_BOOKINGS';
                throw err;
            }
            const list = read('customers').filter(c => c.id !== Number(id));
            write('customers', list);
            return true;
        },

        /* bookings */
        getBookings(){ return read('bookings'); },
        getBookingsByCustomer(id){ return read('bookings').filter(b => b.customerId === Number(id)); },
        getBookingsByStatus(status){ return read('bookings').filter(b => b.status === status); },
        createFlightBooking(payload){
            const list = read('bookings');
            const newId = list.length ? Math.max(...list.map(x=>x.id)) + 1 : 1;
            const obj = {
                id: newId,
                type: 'FLIGHT',
                customerId: Number(payload.customerId),
                bookingDate: payload.bookingDate,
                totalPrice: Number(payload.totalPrice || 0),
                status: payload.status || 'PENDING',
                createdAt: Util.nowISO(),
                flightNumber: payload.flightNumber,
                origin: payload.origin,
                destination: payload.destination,
                seatClass: payload.seatClass
            };
            list.push(obj); write('bookings', list);
            return obj;
        },
        createHotelBooking(payload){
            const list = read('bookings');
            const newId = list.length ? Math.max(...list.map(x=>x.id)) + 1 : 1;
            const obj = {
                id: newId,
                type: 'HOTEL',
                customerId: Number(payload.customerId),
                bookingDate: payload.bookingDate,
                totalPrice: Number(payload.totalPrice || 0),
                status: payload.status || 'PENDING',
                createdAt: Util.nowISO(),
                hotelName: payload.hotelName,
                roomType: payload.roomType,
                nights: Number(payload.nights || 1)
            };
            list.push(obj); write('bookings', list);
            return obj;
        },
        updateBookingStatus(id, status){
            const list = read('bookings');
            const idx = list.findIndex(b => b.id === Number(id));
            if (idx === -1) throw new Error('Booking not found');
            list[idx].status = status;
            write('bookings', list);
            return list[idx];
        },
        deleteBooking(id){
            const list = read('bookings').filter(b => b.id !== Number(id));
            write('bookings', list);
            return true;
        },

        /* packages */
        getPackages(){ return read('packages'); }
    };
})();

/* ----------------------------- API wrapper ----------------------------- */
const Api = (function(){
    const base = CONFIG.API_BASE;

    async function timeoutFetch(resource, options = {}) {
        const controller = new AbortController();
        const id = setTimeout(()=> controller.abort(), CONFIG.FETCH_TIMEOUT_MS);
        try {
            const res = await fetch(resource, { signal: controller.signal, ...options });
            clearTimeout(id);
            return res;
        } catch (err) {
            clearTimeout(id);
            throw err;
        }
    }

    async function handleResponse(res) {
        if (!res.ok) {
            let msg = res.statusText || 'HTTP error';
            try { const j = await res.json(); if (j && j.message) msg = j.message; } catch (e) {}
            const err = new Error(msg);
            err.status = res.status;
            throw err;
        }
        // some endpoints return empty body (204)
        const text = await res.text();
        if (!text) return null;
        try { return JSON.parse(text); } catch(e) { return text; }
    }

    // Generic fetch wrapper with JSON headers and normalized errors
    async function call(method, path, body = null){
        const url = `${base}${path}`;
        const opts = {
            method,
            headers: { 'Content-Type': 'application/json' },
            body: body ? JSON.stringify(body) : undefined
        };
        try {
            const res = await timeoutFetch(url, opts);
            return await handleResponse(res);
        } catch (err) {
            // normalize network errors
            const e = new Error(err.message || 'Network error');
            e.isNetwork = err.name === 'AbortError' || err instanceof TypeError;
            e.original = err;
            throw e;
        }
    }

    // Methods that correspond to required endpoints
    return {
        async getCustomers(){ return await call('GET', '/customers'); },
        async getCustomer(id){ return await call('GET', `/customers/${id}`); },
        async createCustomer(payload){ return await call('POST', '/customers', payload); },
        async updateCustomer(id,payload){ return await call('PUT', `/customers/${id}`, payload); },
        async deleteCustomer(id){ return await call('DELETE', `/customers/${id}`); },

        async getBookings(){ return await call('GET', '/bookings'); },
        async getBookingsByCustomer(id){ return await call('GET', `/bookings/customer/${id}`); },
        async getBookingsByStatus(status){ return await call('GET', `/bookings/status/${status}`); },

        async createFlightBooking(payload){ return await call('POST', '/bookings/flight', payload); },
        async createHotelBooking(payload){ return await call('POST', '/bookings/hotel', payload); },

        async confirmBooking(id){ return await call('PUT', `/bookings/${id}/confirm`); },
        async cancelBooking(id){ return await call('PUT', `/bookings/${id}/cancel`); },
        async deleteBooking(id){ return await call('DELETE', `/bookings/${id}`); }
    };
})();

/* ----------------------------- Global state ----------------------------- */
const State = (function(){
    const state = {
        apiAvailable: true,
        usingLocal: false,
        customers: [],
        bookings: [],
        packages: [],
        ui: {
            selectedTab: 'customers',
            selectedCustomerId: null,
            loading: false,
            filters: { status: '', type: '', q: '' }
        }
    };

    function set(patch){
        Object.assign(state, patch);
        // rerender only required parts (simple approach: full render)
        Renderer.render();
    }

    function patchUi(patch){
        Object.assign(state.ui, patch);
        Renderer.renderUiOnly();
    }

    function get(){ return state; }

    return { get, set, patchUi };
})();

/* ----------------------------- Notifications ----------------------------- */
const Notif = (function(){
    const toast = document.getElementById('toast');
    let tTimer = null;
    function show(msg, type='info', timeout=3000){
        if (!toast) return;
        toast.textContent = msg;
        toast.className = 'toast';
        toast.classList.toggle('hidden', false);
        if (type === 'success') toast.style.background = 'linear-gradient(90deg,#ecfccb,#d1fae5)';
        else if (type === 'error') toast.style.background = 'linear-gradient(90deg,#fee2e2,#fde68a)';
        else toast.style.background = 'rgba(15,23,42,0.95)';
        clearTimeout(tTimer);
        tTimer = setTimeout(()=>{ toast.classList.add('hidden'); }, timeout);
        console.debug('TOAST', type, msg);
    }
    return { show, error(msg){ show(msg,'error',5000); }, success(msg){ show(msg,'success',3000); } };
})();

/* ----------------------------- Renderer (UI) ----------------------------- */
const Renderer = (function(){

    /* helpers to get elements */
    const el = id => document.getElementById(id);
    function showPage(name){
        document.querySelectorAll('.page').forEach(p => p.classList.add('hidden'));
        const target = el('page-' + name);
        if (target) target.classList.remove('hidden');
        // set active nav
        document.querySelectorAll('.nav-link').forEach(a => a.classList.toggle('active', a.dataset.tab === name));
        State.patchUi({ selectedTab: name });
        // push hash without scrolling
        if (location.hash !== '#' + name) history.replaceState(null, '', '#' + name);
    }

    function renderStats(){
        const s = State.get();
        const customers = s.customers || [];
        const bookings = s.bookings || [];
        const revenue = bookings.filter(b => b.status !== 'CANCELLED').reduce((sum, b) => sum + Number(b.totalPrice || b.price || 0), 0);
        const flights = bookings.filter(b => b.type === 'FLIGHT').length;
        el('statTotalCustomers').textContent = customers.length;
        el('statTotalBookings').textContent = bookings.length;
        el('statRevenue').textContent = Util.formatCurrency(revenue);
        el('statFlights').textContent = flights;
    }

    function renderCustomersTable(){
        const s = State.get();
        const tbody = el('customersTBody');
        const customers = s.customers || [];
        const q = (document.getElementById('searchCustomers')?.value || '').trim().toLowerCase();
        const filtered = customers.filter(c => !q || c.name.toLowerCase().includes(q) || c.email.toLowerCase().includes(q));
        if (!filtered.length){
            document.getElementById('customersEmpty').classList.remove('hidden');
            document.getElementById('customersTableWrap').classList.add('hidden');
            return;
        } else {
            document.getElementById('customersEmpty').classList.add('hidden');
            document.getElementById('customersTableWrap').classList.remove('hidden');
        }

        tbody.innerHTML = filtered.map(c => `
            <tr>
                <td>${c.id}</td>
                <td><strong>${Util.escape(c.name)}</strong></td>
                <td>${Util.escape(c.email)}</td>
                <td>${Util.escape(c.phone)}</td>
                <td>${Util.escape(c.passportNumber || c.passport || '')}</td>
                <td>${(c.createdAt || c.created_at || '').slice(0,10)}</td>
                <td>
                    <button class="btn small" data-cmd="edit-customer" data-id="${c.id}">Edit</button>
                    <button class="btn small muted" data-cmd="delete-customer" data-id="${c.id}">Delete</button>
                    <button class="btn small muted" data-cmd="select-customer" data-id="${c.id}">Use</button>
                </td>
            </tr>
        `).join('');
    }

    function renderBookingsTable(){
        const s = State.get();
        const tbody = el('bookingsTBody');
        let bookings = s.bookings || [];
        // apply filters
        const status = document.getElementById('filterByStatus')?.value || '';
        const type = document.getElementById('filterByType')?.value || '';
        const q = (document.getElementById('searchBookings')?.value || '').trim().toLowerCase();
        if (status) bookings = bookings.filter(b => b.status === status);
        if (type) bookings = bookings.filter(b => b.type === type);
        if (q) {
            bookings = bookings.filter(b => {
                const cust = s.customers.find(c => c.id === b.customerId) || {};
                return (cust.name || '').toLowerCase().includes(q) || (b.flightNumber || '').toLowerCase().includes(q) || (b.hotelName || '').toLowerCase().includes(q) || (cust.email || '').toLowerCase().includes(q);
            });
        }
        if (!bookings.length){
            document.getElementById('bookingsEmpty').classList.remove('hidden');
            document.getElementById('bookingsTable').classList.add('hidden');
            return;
        } else {
            document.getElementById('bookingsEmpty').classList.add('hidden');
            document.getElementById('bookingsTable').classList.remove('hidden');
        }

        tbody.innerHTML = bookings.map(b => {
            const cust = s.customers.find(c => c.id === b.customerId) || { name: 'Unknown' };
            const details = b.type === 'FLIGHT' ? `${Util.escape(b.flightNumber || b.flight_number || '')} ${Util.escape(b.origin||'')} → ${Util.escape(b.destination||'')}` : `${Util.escape(b.hotelName||b.hotel_name||'')} (${b.nights||''} nights)`;
            const price = Util.formatCurrency(b.totalPrice || b.total_price || b.price || 0);
            // actions depending on status
            let actions = '';
            if (b.status === 'PENDING'){
                actions += `<button class="btn small" data-cmd="confirm-booking" data-id="${b.id}">Confirm</button> `;
                actions += `<button class="btn small muted" data-cmd="cancel-booking" data-id="${b.id}">Cancel</button> `;
            } else if (b.status === 'CONFIRMED'){
                actions += `<button class="btn small muted" data-cmd="cancel-booking" data-id="${b.id}">Cancel</button> `;
            } else {
                actions += `<button class="btn small muted" disabled>—</button> `;
            }
            actions += `<button class="btn small muted" data-cmd="delete-booking" data-id="${b.id}">Delete</button>`;
            return `
                <tr>
                    <td>${b.id}</td>
                    <td>${b.type}</td>
                    <td>${Util.escape(cust.name)}</td>
                    <td>${Util.escape(b.bookingDate || b.booking_date || b.date || '')}</td>
                    <td>${details}</td>
                    <td>${price}</td>
                    <td>${b.status}</td>
                    <td>${actions}</td>
                </tr>
            `;
        }).join('');
    }

    function renderPackages(){
        const s = State.get();
        const grid = document.getElementById('packagesGrid');
        const pkgs = s.packages || [];
        if (!pkgs.length) {
            grid.innerHTML = `<div class="empty-state"><p>No packages available</p></div>`;
            return;
        }
        grid.innerHTML = pkgs.map(p=>{
            const cust = s.customers.find(c => c.id === p.customerId) || { name: 'Unknown' };
            const totalBefore = (p.bookingIds || []).reduce((sum, id) => {
                const b = s.bookings.find(x=>x.id === id) || {};
                return sum + Number(b.totalPrice || b.total_price || b.price || 0);
            }, 0);
            const after = (totalBefore * (1 - (Number(p.discount || p.discount_percentage || 0)/100))).toFixed(2);
            return `<div class="package-card">
                <div style="font-weight:700">${Util.escape(p.name)} <small style="color:var(--muted)">by ${Util.escape(cust.name)}</small></div>
                <div style="margin-top:8px">Items: ${(p.bookingIds||[]).length} — <strong>${Util.formatCurrency(after)}</strong></div>
            </div>`;
        }).join('');
    }

    function renderCustomerDropdowns(){
        const s = State.get();
        const selHtml = `<option value="">Select customer</option>` + (s.customers || []).map(c => `<option value="${c.id}">${Util.escape(c.name)} (${Util.escape(c.email)})</option>`).join('');
        document.querySelectorAll('.customer-select').forEach(sel=>{
            const prev = sel.value;
            sel.innerHTML = selHtml;
            if (prev) sel.value = prev;
            // if state has selectedCustomerId set, force selection
            const selectedId = s.ui.selectedCustomerId;
            if (selectedId && sel.contains(document.querySelector(`option[value="${selectedId}"]`))) sel.value = selectedId;
        });
        // global select
        const gs = document.getElementById('globalCustomerSelect');
        if (gs){
            const prev = gs.value;
            gs.innerHTML = `<option value="">Select customer...</option>` + (s.customers || []).map(c => `<option value="${c.id}">${Util.escape(c.name)} — ${Util.escape(c.email)}</option>`).join('');
            if (prev) gs.value = prev;
        }
    }

    function renderUiOnly(){
        renderStats();
        renderCustomerDropdowns();
        renderCustomersTable();
        renderBookingsTable();
        renderPackages();
    }

    function render(){
        renderUiOnly();
        // handle page selection
        showPage(State.get().ui.selectedTab || 'customers');
    }

    return { render, renderUiOnly, showPage };
})();

/* ----------------------------- App logic / event wiring ----------------------------- */
const App = (function(){
    const state = State;
    const api = Api;
    const local = LocalDB;

    // INITIALIZATION
    async function init(){
        local.init();

        // try API; if fails, fallback to local
        try {
            await loadAllFromApi();
            state.set({ apiAvailable:true, usingLocal:false });
            document.getElementById('apiOverlay').classList.add('hidden');
        } catch (err) {
            console.warn('API not available, using local DB:', err);
            state.set({ apiAvailable:false, usingLocal:true });
            // load local data from LocalDB
            state.set({
                customers: local.getCustomers(),
                bookings: local.getBookings(),
                packages: local.getPackages()
            });
            // show overlay
            document.getElementById('apiOverlay').classList.remove('hidden');
        }

        // wire UI events
        wireUi();
        Renderer.render();
    }

    /* ----------------------------- Data loading ----------------------------- */
    async function loadAllFromApi(){
        // fetch customers/bookings/packages in parallel
        const [customers, bookings] = await Promise.all([ api.getCustomers(), api.getBookings() ]);
        // normalize fields if necessary (passportNumber)
        const normCustomers = (customers || []).map(c => ({ ...c }));
        state.set({ customers: normCustomers, bookings: (bookings || []), packages: [] });
    }

    /* ----------------------------- UI actions ----------------------------- */
    function wireUi(){
        // nav links (hash)
        document.querySelectorAll('.nav-link').forEach(a=>{
            a.addEventListener('click', (e)=>{
                e.preventDefault();
                const tab = a.dataset.tab;
                State.patchUi({ selectedTab: tab });
                Renderer.showPage(tab);
            });
        });

        // search customers debounce
        document.getElementById('searchCustomers')?.addEventListener('input', Util.debounce(()=>Renderer.renderUiOnly(), 200));
        document.getElementById('searchBookings')?.addEventListener('input', Util.debounce(()=>Renderer.renderUiOnly(), 200));

        // filter change
        document.getElementById('filterByStatus')?.addEventListener('change', ()=>Renderer.renderUiOnly());
        document.getElementById('filterByType')?.addEventListener('change', ()=>Renderer.renderUiOnly());

        // Add customer modal open
        document.getElementById('openAddCustomer')?.addEventListener('click', ()=>openCustomerModal());
        document.getElementById('ctaAddCustomer')?.addEventListener('click', ()=>openCustomerModal());
        document.getElementById('ctaNewBooking')?.addEventListener('click', ()=>{ Renderer.showPage('new-booking'); });

        // modal cancel
        document.getElementById('modalCancel')?.addEventListener('click', closeCustomerModal);

        // modal save
        document.getElementById('modalCustomerForm')?.addEventListener('submit', onModalCustomerSave);

        // bookings forms
        document.getElementById('formFlight')?.addEventListener('submit', onCreateFlight);
        document.getElementById('formHotel')?.addEventListener('submit', onCreateHotel);

        // global customer select focus
        document.getElementById('btnFocusCustomer')?.addEventListener('click', ()=>document.getElementById('globalCustomerSelect')?.focus());

        // export CSV
        document.getElementById('btnExportCsv')?.addEventListener('click', exportCsv);

        // retry
        document.getElementById('btnRetryApi')?.addEventListener('click', retryApi);
        document.getElementById('overlayRetry')?.addEventListener('click', retryApi);
        document.getElementById('overlayUseLocal')?.addEventListener('click', ()=>{ document.getElementById('apiOverlay').classList.add('hidden'); });

        // table button delegation
        document.getElementById('customersTBody')?.addEventListener('click', onCustomersTableClick);
        document.getElementById('bookingsTBody')?.addEventListener('click', onBookingsTableClick);

        // package creation placeholder
        document.getElementById('btnNewPackage')?.addEventListener('click', ()=>Notif.show('Package creation will be implemented on backend'));

        // hash on load
        window.addEventListener('load', () => {
            const hash = location.hash.replace('#','') || 'customers';
            State.patchUi({ selectedTab: hash });
            Renderer.showPage(hash);
        });
        // hashchange
        window.addEventListener('hashchange', ()=> {
            const hash = location.hash.replace('#','') || 'customers';
            State.patchUi({ selectedTab: hash });
            Renderer.showPage(hash);
        });
    }

    /* ----------------------------- Modal: Add/Edit Customer ----------------------------- */
    function openCustomerModal(customer = null){
        const modal = document.getElementById('modalCustomer');
        modal.classList.remove('hidden');
        document.getElementById('modalCustomerTitle').textContent = customer ? 'Edit customer' : 'Add customer';
        document.getElementById('modalCustomerId').value = customer?.id || '';
        document.getElementById('modalName').value = customer?.name || '';
        document.getElementById('modalEmail').value = customer?.email || '';
        document.getElementById('modalPhone').value = customer?.phone || '';
        document.getElementById('modalPassport').value = customer?.passportNumber || customer?.passport || '';
    }
    function closeCustomerModal(){ document.getElementById('modalCustomer').classList.add('hidden'); }

    async function onModalCustomerSave(e){
        e.preventDefault();
        const id = document.getElementById('modalCustomerId').value;
        const payload = {
            name: document.getElementById('modalName').value.trim(),
            email: document.getElementById('modalEmail').value.trim(),
            phone: document.getElementById('modalPhone').value.trim(),
            passportNumber: document.getElementById('modalPassport').value.trim()
        };
        try {
            if (id) {
                // update
                if (State.get().apiAvailable && !State.get().usingLocal) {
                    await Api.updateCustomer(id, payload);
                    await loadAllAndRender();
                    Notif.success('Customer updated');
                } else {
                    const result = LocalDB.updateCustomer(id, payload);
                    State.set({ customers: LocalDB.getCustomers() });
                    Notif.success('Customer updated (local)');
                }
            } else {
                // create
                let created;
                if (State.get().apiAvailable && !State.get().usingLocal) {
                    created = await Api.createCustomer({
                        name: payload.name,
                        email: payload.email,
                        phone: payload.phone,
                        passportNumber: payload.passportNumber
                    });
                    await loadAllAndRender();
                    Notif.success('Customer created');
                } else {
                    created = LocalDB.createCustomer({
                        name: payload.name,
                        email: payload.email,
                        phone: payload.phone,
                        passportNumber: payload.passportNumber
                    });
                    State.set({ customers: LocalDB.getCustomers() });
                    Notif.success('Customer created (local)');
                }
                // automatically select created customer and go to new booking
                State.patchUi({ selectedCustomerId: created.id, selectedTab: 'new-booking' });
                Renderer.showPage('new-booking');
                // set selects after small delay
                setTimeout(()=> {
                    document.querySelectorAll('.customer-select').forEach(s => s.value = created.id);
                    document.getElementById('globalCustomerSelect') && (document.getElementById('globalCustomerSelect').value = created.id);
                }, 120);
            }
            closeCustomerModal();
            Renderer.renderUiOnly();
        } catch (err){
            console.error(err);
            Notif.error(err.message || 'Save failed');
        }
    }

    /* ----------------------------- Create Bookings ----------------------------- */
    async function onCreateFlight(e){
        e.preventDefault();
        const payload = {
            customerId: Number(document.getElementById('flightCustomer').value || document.getElementById('globalCustomerSelect').value),
            bookingDate: document.getElementById('flightDate').value || Util.todayDate(),
            totalPrice: Number(document.getElementById('flightPrice').value || 0),
            status: 'PENDING',
            flightNumber: document.getElementById('flightNumber').value.trim(),
            origin: document.getElementById('flightOrigin').value.trim(),
            destination: document.getElementById('flightDestination').value.trim(),
            seatClass: document.getElementById('flightClass').value
        };
        if (!payload.customerId){ Notif.error('Select a customer'); return; }
        try {
            if (State.get().apiAvailable && !State.get().usingLocal) {
                await Api.createFlightBooking(payload);
                await loadAllAndRender();
                Notif.success('Flight booking created (PENDING)');
            } else {
                LocalDB.createFlightBooking(payload);
                State.set({ bookings: LocalDB.getBookings() });
                Notif.success('Flight booking created (local)');
            }
            // switch to bookings
            State.patchUi({ selectedTab: 'bookings' });
            Renderer.showPage('bookings');
        } catch(err){
            console.error(err);
            Notif.error(err.message || 'Create booking failed');
        }
    }

    async function onCreateHotel(e){
        e.preventDefault();
        const payload = {
            customerId: Number(document.getElementById('hotelCustomer').value || document.getElementById('globalCustomerSelect').value),
            bookingDate: document.getElementById('hotelDate').value || Util.todayDate(),
            totalPrice: Number(document.getElementById('hotelPrice').value || 0),
            status: 'PENDING',
            hotelName: document.getElementById('hotelName').value.trim(),
            roomType: document.getElementById('hotelRoomType').value,
            nights: Number(document.getElementById('hotelNights').value || 1)
        };
        if (!payload.customerId){ Notif.error('Select a customer'); return; }
        try {
            if (State.get().apiAvailable && !State.get().usingLocal) {
                await Api.createHotelBooking(payload);
                await loadAllAndRender();
                Notif.success('Hotel booking created (PENDING)');
            } else {
                LocalDB.createHotelBooking(payload);
                State.set({ bookings: LocalDB.getBookings() });
                Notif.success('Hotel booking created (local)');
            }
            // switch to bookings
            State.patchUi({ selectedTab: 'bookings' });
            Renderer.showPage('bookings');
        } catch(err){
            console.error(err);
            Notif.error(err.message || 'Create booking failed');
        }
    }

    /* ----------------------------- Bookings table actions ----------------------------- */
    async function onBookingsTableClick(e){
        const btn = e.target.closest('button');
        if (!btn) return;
        const cmd = btn.dataset.cmd;
        const id = btn.dataset.id;
        if (!cmd || !id) return;
        try {
            if (cmd === 'confirm-booking'){
                if (State.get().apiAvailable && !State.get().usingLocal){
                    await Api.confirmBooking(id);
                    await loadAllAndRender();
                } else {
                    LocalDB.updateBookingStatus(id, 'CONFIRMED');
                    State.set({ bookings: LocalDB.getBookings() });
                }
                Notif.success('Booking confirmed');
            } else if (cmd === 'cancel-booking'){
                if (State.get().apiAvailable && !State.get().usingLocal){
                    await Api.cancelBooking(id);
                    await loadAllAndRender();
                } else {
                    LocalDB.updateBookingStatus(id, 'CANCELLED');
                    State.set({ bookings: LocalDB.getBookings() });
                }
                Notif.success('Booking cancelled');
            } else if (cmd === 'delete-booking'){
                if (!confirm('Delete booking?')) return;
                if (State.get().apiAvailable && !State.get().usingLocal){
                    await Api.deleteBooking(id);
                    await loadAllAndRender();
                } else {
                    LocalDB.deleteBooking(id);
                    State.set({ bookings: LocalDB.getBookings() });
                }
                Notif.success('Booking deleted');
            }
            Renderer.renderUiOnly();
        } catch(err){
            console.error(err);
            Notif.error(err.message || 'Action failed');
        }
    }

    /* ----------------------------- Customers table actions ----------------------------- */
    function onCustomersTableClick(e){
        const btn = e.target.closest('button');
        if (!btn) return;
        const cmd = btn.dataset.cmd;
        const id = btn.dataset.id;
        if (!cmd || !id) return;

        if (cmd === 'edit-customer'){
            const customer = (State.get().customers || []).find(c => c.id === Number(id));
            if (!customer) return Notif.error('Customer not found');
            openCustomerModal(customer);
        } else if (cmd === 'delete-customer'){
            if (!confirm('Delete customer? This will be blocked if customer has bookings.')) return;
            (async ()=>{
                try {
                    if (State.get().apiAvailable && !State.get().usingLocal){
                        await Api.deleteCustomer(id);
                        await loadAllAndRender();
                    } else {
                        try {
                            LocalDB.deleteCustomer(id);
                            State.set({ customers: LocalDB.getCustomers() });
                        } catch(err){
                            if (err.code === 'HAS_BOOKINGS') throw new Error('Cannot delete customer with bookings');
                            throw err;
                        }
                    }
                    Notif.success('Customer deleted');
                    Renderer.renderUiOnly();
                } catch(err){
                    console.error(err);
                    Notif.error(err.message || 'Delete failed');
                }
            })();
        } else if (cmd === 'select-customer'){
            // choose this customer for new booking forms and global selector
            State.patchUi({ selectedCustomerId: Number(id), selectedTab: 'new-booking' });
            Renderer.showPage('new-booking');
            setTimeout(()=> {
                document.querySelectorAll('.customer-select').forEach(s => { if (s) s.value = id; });
                document.getElementById('globalCustomerSelect') && (document.getElementById('globalCustomerSelect').value = id);
            }, 80);
        }
    }

    /* ----------------------------- Helpers ----------------------------- */
    async function loadAllAndRender(){
        if (State.get().apiAvailable && !State.get().usingLocal) {
            try {
                await loadAllFromApi();
            } catch(err){
                console.warn('Switching to local due API error', err);
                State.set({ apiAvailable:false, usingLocal:true });
                LocalDB.init();
                State.set({ customers: LocalDB.getCustomers(), bookings: LocalDB.getBookings(), packages: LocalDB.getPackages() });
                document.getElementById('apiOverlay').classList.remove('hidden');
            }
        } else {
            LocalDB.init();
            State.set({ customers: LocalDB.getCustomers(), bookings: LocalDB.getBookings(), packages: LocalDB.getPackages() });
        }
    }

    async function retryApi(){
        try {
            await loadAllFromApi();
            State.set({ apiAvailable:true, usingLocal:false });
            document.getElementById('apiOverlay').classList.add('hidden');
            Notif.success('API reachable, using live backend');
        } catch(err){
            Notif.error('API still unavailable');
        }
    }

    async function loadAllFromApi(){
        const [customers, bookings] = await Promise.all([ Api.getCustomers(), Api.getBookings() ]);
        State.set({ customers: customers || [], bookings: bookings || [], packages: [] });
    }

    /* ----------------------------- Export CSV ----------------------------- */
    function exportCsv(){
        const s = State.get();
        const customers = s.customers || [];
        const bookings = s.bookings || [];
        // compile customers CSV
        const custCsv = ['id,name,email,phone,passportNumber,createdAt', ...customers.map(c => `${c.id},"${(c.name||'').replace(/"/g,'""')}","${(c.email||'').replace(/"/g,'""')}","${(c.phone||'').replace(/"/g,'""')}","${(c.passportNumber||'').replace(/"/g,'""')}",${c.createdAt||''}`)].join('\n');
        Util.download('customers.csv', custCsv);
        Notif.success('Customers exported');
    }

    return { init };
})();

/* ----------------------------- Start app ----------------------------- */
document.addEventListener('DOMContentLoaded', () => {
    App.init().catch(e => {
        console.error('Initialization error', e);
        Notif.error('Initialization failed');
    });
});
