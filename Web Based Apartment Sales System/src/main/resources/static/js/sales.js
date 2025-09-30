const api = {
  leads: '/api/leads',
  interactions: '/api/interactions',
  sales: '/api/sales',
  report: '/api/reports/summary'
};

// Helpers
const $ = (sel) => document.querySelector(sel);
const $$ = (sel) => document.querySelectorAll(sel);

async function json(url, opts={}) {
  const res = await fetch(url, {headers:{'Content-Type':'application/json'}, ...opts});
  if(!res.ok) throw new Error(await res.text());
  return res.json();
}

/* ===== Leads ===== */
async function loadLeads() {
  const status = $('#leadStatusFilter').value;
  const url = status ? `${api.leads}/status/${status}` : api.leads;
  const data = await json(url);
  const tbody = $('#leadTable tbody');
  tbody.innerHTML = '';
  for (const l of data) {
    const tr = document.createElement('tr');
    tr.innerHTML = `
      <td>${l.leadId}</td>
      <td>${l.customer.firstName} ${l.customer.lastName}</td>
      <td>${l.customer.email}</td>
      <td>${l.customer.phone ?? ''}</td>
      <td>${l.source ?? ''}</td>
      <td>
        <select data-lead="${l.leadId}" class="statusSelect">
          ${['NEW','CONTACTED','QUALIFIED','LOST','CONVERTED'].map(s =>
            `<option ${l.status===s?'selected':''}>${s}</option>`).join('')}
        </select>
      </td>
      <td><input class="assignee" data-lead="${l.leadId}" value="${l.assignedTo ?? ''}" placeholder="assign to"/></td>
      <td>${l.apartmentCode ?? ''}</td>
      <td>
        <button class="btn-assign" data-lead="${l.leadId}">Assign</button>
      </td>`;
    tbody.appendChild(tr);
  }

  // bind events
  $$('.statusSelect').forEach(sel => {
    sel.addEventListener('change', async e => {
      const id = e.target.getAttribute('data-lead');
      await json(`${api.leads}/${id}`, {
        method:'PUT',
        body: JSON.stringify({ status: e.target.value })
      });
    });
  });
  $$('.btn-assign').forEach(btn => {
    btn.addEventListener('click', async e => {
      const id = e.target.getAttribute('data-lead');
      const input = document.querySelector(`input.assignee[data-lead="${id}"]`);
      await json(`${api.leads}/${id}/assign?to=${encodeURIComponent(input.value)}`, { method:'PUT' });
      alert('Assigned!');
    });
  });
}

$('#leadForm').addEventListener('submit', async (e)=>{
  e.preventDefault();
  const fd = new FormData(e.target);
  const payload = {
    customer: {
      firstName: fd.get('firstName'),
      lastName: fd.get('lastName'),
      email: fd.get('email'),
      phone: fd.get('phone')
    },
    source: fd.get('source'),
    assignedTo: fd.get('assignedTo'),
    apartmentCode: fd.get('apartmentCode'),
    budget: fd.get('budget') ? Number(fd.get('budget')) : null
  };
  await json(api.leads, { method:'POST', body: JSON.stringify(payload) });
  e.target.reset();
  await loadLeads();
});

$('#refreshLeads').addEventListener('click', loadLeads);
$('#leadStatusFilter').addEventListener('change', loadLeads);

/* ===== Interactions ===== */
$('#interactionForm').addEventListener('submit', async (e)=>{
  e.preventDefault();
  const fd = new FormData(e.target);
  const payload = {
    lead: { leadId: Number(fd.get('leadId')) },
    channel: fd.get('channel'),
    notes: fd.get('notes')
  };
  await json(api.interactions, { method:'POST', body: JSON.stringify(payload) });
  alert('Interaction added');
  $('#loadInteractions').click();
  e.target.reset();
});

$('#loadInteractions').addEventListener('click', async ()=>{
  const leadId = Number($('#intLeadId').value);
  if(!leadId) return;
  const data = await json(`${api.interactions}/lead/${leadId}`);
  const tbody = $('#interactionTable tbody');
  tbody.innerHTML = '';
  for(const i of data){
    const tr = document.createElement('tr');
    tr.innerHTML = `<td>${i.interactionId}</td><td>${i.lead.leadId}</td><td>${i.channel}</td><td>${i.notes}</td><td>${(i.interactionDate||'').toString().replace('T',' ')}</td>`;
    tbody.appendChild(tr);
  }
});

/* ===== Sales ===== */
$('#saleForm').addEventListener('submit', async (e)=>{
  e.preventDefault();
  const fd = new FormData(e.target);
  const payload = {
    lead: { leadId: Number(fd.get('leadId')) },
    apartmentCode: fd.get('apartmentCode'),
    price: fd.get('price') ? Number(fd.get('price')) : null,
    status: fd.get('status'),
    paymentStatus: fd.get('paymentStatus'),
    contractDate: fd.get('contractDate') || null
  };
  await json(api.sales, { method:'POST', body: JSON.stringify(payload) });
  alert('Sale created');
  await loadSales();
  e.target.reset();
});

async function loadSales(){
  const data = await json(api.sales);
  const tbody = $('#saleTable tbody');
  tbody.innerHTML = '';
  for(const s of data){
    const tr = document.createElement('tr');
    tr.innerHTML = `
      <td>${s.saleId}</td>
      <td>${s.lead?.leadId ?? ''}</td>
      <td>${s.apartmentCode ?? ''}</td>
      <td>${s.price ?? ''}</td>
      <td>${s.status}</td>
      <td>${s.paymentStatus}</td>
      <td>${s.contractDate ?? ''}</td>`;
    tbody.appendChild(tr);
  }
}
$('#refreshSales').addEventListener('click', loadSales);

/* ===== Reports ===== */
$('#loadReport').addEventListener('click', async ()=>{
  const y = Number($('#reportYear').value);
  const data = await json(`${api.report}?year=${y}`);
  $('#reportBox').textContent = JSON.stringify(data, null, 2);
});

// initial loads
loadLeads();
loadSales();
