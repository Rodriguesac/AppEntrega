import { initializeApp } from 'https://www.gstatic.com/firebasejs/10.12.5/firebase-app.js';
import {
  getFirestore,
  collection,
  addDoc,
  updateDoc,
  deleteDoc,
  doc,
  onSnapshot,
  serverTimestamp,
  Timestamp
} from 'https://www.gstatic.com/firebasejs/10.12.5/firebase-firestore.js';

const firebaseConfig = {
  apiKey: 'COLE_SUA_API_KEY',
  authDomain: 'COLE_SEU_AUTH_DOMAIN',
  projectId: 'COLE_SEU_PROJECT_ID',
  storageBucket: 'COLE_SEU_STORAGE_BUCKET',
  messagingSenderId: 'COLE_SEU_MESSAGING_SENDER_ID',
  appId: 'COLE_SEU_APP_ID'
};

const cloudinaryConfig = {
  cloudName: 'COLE_SEU_CLOUD_NAME',
  uploadPreset: 'COLE_SEU_UNSIGNED_UPLOAD_PRESET',
  folder: 'up-entregas/carrossel-entregador'
};

const app = initializeApp(firebaseConfig);
const db = getFirestore(app);
const bannersRef = collection(db, 'app_carousel_banners');

const $ = (id) => document.getElementById(id);
const state = { banners: [], currentImageUrl: '' };

const fields = {
  id: $('bannerId'),
  imageFile: $('imageFile'),
  preview: $('imagePreview'),
  title: $('title'),
  badge: $('badge'),
  description: $('description'),
  buttonText: $('buttonText'),
  order: $('order'),
  actionType: $('actionType'),
  actionValue: $('actionValue'),
  startsAt: $('startsAt'),
  endsAt: $('endsAt'),
  active: $('active'),
  status: $('status'),
  list: $('bannerList'),
  formMode: $('formMode'),
  appPreview: $('appPreview')
};

function setStatus(message, error = false) {
  fields.status.textContent = message;
  fields.status.style.color = error ? '#d92d4a' : '#2e7d00';
}

function toDateInput(value) {
  if (!value) return '';
  const date = value.toDate ? value.toDate() : new Date(value);
  return Number.isNaN(date.getTime()) ? '' : date.toISOString().slice(0, 10);
}

function dateToTimestamp(value, endOfDay = false) {
  if (!value) return null;
  const date = new Date(value + (endOfDay ? 'T23:59:59' : 'T00:00:00'));
  return Timestamp.fromDate(date);
}

async function uploadToCloudinary(file) {
  if (!file) return state.currentImageUrl;
  if (cloudinaryConfig.cloudName.includes('COLE_') || cloudinaryConfig.uploadPreset.includes('COLE_')) {
    throw new Error('Configure cloudName e uploadPreset do Cloudinary em gestor-up-entregas/app.js.');
  }
  const payload = new FormData();
  payload.append('file', file);
  payload.append('upload_preset', cloudinaryConfig.uploadPreset);
  payload.append('folder', cloudinaryConfig.folder);

  const response = await fetch(`https://api.cloudinary.com/v1_1/${cloudinaryConfig.cloudName}/image/upload`, {
    method: 'POST',
    body: payload
  });
  const json = await response.json();
  if (!response.ok) throw new Error(json.error?.message || 'Falha ao enviar imagem para Cloudinary.');
  return json.secure_url;
}

function renderPreview() {
  const title = fields.title.value || 'Novidades da operação';
  const badge = fields.badge.value || 'NOVIDADES';
  const description = fields.description.value || 'Fique por dentro das atualizações importantes.';
  const button = fields.buttonText.value || 'Saiba mais';
  fields.appPreview.innerHTML = `<div><b>${escapeHtml(badge)}</b><strong>${escapeHtml(title)}</strong><span>${escapeHtml(description)}</span><button>${escapeHtml(button)}</button></div><i>up</i>`;
  fields.appPreview.classList.toggle('bg-image', Boolean(state.currentImageUrl));
  fields.appPreview.style.backgroundImage = state.currentImageUrl ? `url('${state.currentImageUrl}')` : '';
}

function escapeHtml(value) {
  return String(value).replace(/[&<>'"]/g, (char) => ({ '&':'&amp;', '<':'&lt;', '>':'&gt;', "'":'&#39;', '"':'&quot;' }[char]));
}

function clearForm() {
  fields.id.value = '';
  fields.imageFile.value = '';
  state.currentImageUrl = '';
  fields.preview.classList.add('hidden');
  fields.preview.src = '';
  fields.title.value = '';
  fields.badge.value = '';
  fields.description.value = '';
  fields.buttonText.value = '';
  fields.order.value = Math.max(1, state.banners.length + 1);
  fields.actionType.value = 'none';
  fields.actionValue.value = '';
  fields.startsAt.value = '';
  fields.endsAt.value = '';
  fields.active.checked = true;
  fields.formMode.textContent = 'Adicionar';
  renderPreview();
}

function fillForm(banner) {
  fields.id.value = banner.id;
  state.currentImageUrl = banner.imageUrl || '';
  fields.preview.src = state.currentImageUrl;
  fields.preview.classList.toggle('hidden', !state.currentImageUrl);
  fields.title.value = banner.title || '';
  fields.badge.value = banner.badge || '';
  fields.description.value = banner.description || '';
  fields.buttonText.value = banner.buttonText || '';
  fields.order.value = banner.order || 1;
  fields.actionType.value = banner.actionType || 'none';
  fields.actionValue.value = banner.actionValue || '';
  fields.startsAt.value = toDateInput(banner.startsAt);
  fields.endsAt.value = toDateInput(banner.endsAt);
  fields.active.checked = banner.active !== false;
  fields.formMode.textContent = 'Editando';
  renderPreview();
  window.scrollTo({ top: 0, behavior: 'smooth' });
}

async function saveBanner(event) {
  event.preventDefault();
  setStatus('Salvando banner...');
  try {
    const imageUrl = await uploadToCloudinary(fields.imageFile.files[0]);
    const payload = {
      title: fields.title.value.trim(),
      badge: fields.badge.value.trim() || 'UP ENTREGAS',
      description: fields.description.value.trim(),
      buttonText: fields.buttonText.value.trim() || 'Saiba mais',
      imageUrl,
      cloudinaryUrl: imageUrl,
      order: Number(fields.order.value || 999),
      actionType: fields.actionType.value,
      actionValue: fields.actionValue.value.trim(),
      active: fields.active.checked,
      ativo: fields.active.checked,
      startsAt: dateToTimestamp(fields.startsAt.value),
      endsAt: dateToTimestamp(fields.endsAt.value, true),
      updatedAt: serverTimestamp(),
      atualizadoEm: serverTimestamp()
    };

    if (!payload.imageUrl) throw new Error('Escolha uma imagem ou edite um banner que já possui imagem.');

    if (fields.id.value) {
      await updateDoc(doc(db, 'app_carousel_banners', fields.id.value), payload);
      setStatus('Banner atualizado com sucesso.');
    } else {
      await addDoc(bannersRef, { ...payload, createdAt: serverTimestamp(), criadoEm: serverTimestamp() });
      setStatus('Banner criado com sucesso.');
    }
    clearForm();
  } catch (error) {
    setStatus(error.message || 'Erro ao salvar banner.', true);
  }
}

function renderList() {
  fields.list.innerHTML = '';
  if (!state.banners.length) {
    fields.list.innerHTML = '<div class="banner-row"><span>—</span><span></span><div class="row-title"><strong>Nenhum banner cadastrado</strong><span>Crie o primeiro banner no formulário acima.</span></div></div>';
    return;
  }
  for (const banner of state.banners) {
    const row = document.createElement('div');
    row.className = 'banner-row';
    row.innerHTML = `
      <strong>${banner.order || '—'}</strong>
      <img class="thumb" src="${escapeHtml(banner.imageUrl || '')}" alt="Prévia">
      <div class="row-title"><strong>${escapeHtml(banner.title || 'Sem título')}</strong><span>${escapeHtml(banner.description || '')}</span></div>
      <div class="row-action">${escapeHtml(actionLabel(banner.actionType))}<br>${escapeHtml(banner.actionValue || 'Sem destino')}</div>
      <span class="${banner.active === false ? 'badge-inactive' : 'badge-active'}">${banner.active === false ? 'Inativo' : 'Ativo'}</span>
      <div class="row-buttons"><button class="icon-btn" data-edit="${banner.id}">Editar</button><button class="icon-btn danger" data-delete="${banner.id}">Excluir</button></div>
    `;
    fields.list.appendChild(row);
  }
}

function actionLabel(type) {
  return ({ none:'Sem ação', link:'Abrir link', internal:'Página interna', modal:'Aviso/modal' })[type] || type || 'Sem ação';
}

function listenBanners() {
  onSnapshot(bannersRef, (snapshot) => {
    state.banners = snapshot.docs.map((item) => ({ id: item.id, ...item.data() })).sort((a, b) => (a.order || 999) - (b.order || 999));
    renderList();
  }, (error) => setStatus(error.message || 'Erro ao carregar banners.', true));
}

fields.imageFile.addEventListener('change', () => {
  const file = fields.imageFile.files[0];
  if (!file) return;
  state.currentImageUrl = URL.createObjectURL(file);
  fields.preview.src = state.currentImageUrl;
  fields.preview.classList.remove('hidden');
  renderPreview();
});

['title','badge','description','buttonText'].forEach((id) => $(id).addEventListener('input', renderPreview));
$('bannerForm').addEventListener('submit', saveBanner);
$('resetBtn').addEventListener('click', clearForm);
$('reloadBtn').addEventListener('click', renderList);
fields.list.addEventListener('click', async (event) => {
  const editId = event.target.dataset.edit;
  const deleteId = event.target.dataset.delete;
  if (editId) fillForm(state.banners.find((banner) => banner.id === editId));
  if (deleteId && confirm('Remover este banner?')) {
    await deleteDoc(doc(db, 'app_carousel_banners', deleteId));
    setStatus('Banner removido.');
  }
});

clearForm();
listenBanners();
