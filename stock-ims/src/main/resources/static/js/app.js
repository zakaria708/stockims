document.addEventListener('DOMContentLoaded', () => {
  const button = document.querySelector('[data-menu-toggle]');
  const sidebar = document.querySelector('.sidebar');
  if (button && sidebar) {
    button.addEventListener('click', () => sidebar.classList.toggle('open'));
    document.addEventListener('click', (event) => {
      if (window.innerWidth <= 900 && sidebar.classList.contains('open') &&
          !sidebar.contains(event.target) && !button.contains(event.target)) {
        sidebar.classList.remove('open');
      }
    });
  }

  document.querySelectorAll('form[data-confirm]').forEach((form) => {
    form.addEventListener('submit', (event) => {
      if (!window.confirm(form.dataset.confirm || 'Confirmer cette action ?')) {
        event.preventDefault();
      }
    });
  });

  // Aperçu interactif. Le serveur Java reste l'autorité lors de l'enregistrement.
  const inputStock = document.querySelector('[data-preview-stock]');
  const inputSeuil = document.querySelector('[data-preview-seuil]');
  const preview = document.querySelector('[data-status-preview]');
  if (inputStock && inputSeuil && preview) {
    const actualiser = () => {
      const stock = inputStock.value.trim() === '' ? 0 : Number(inputStock.value);
      const seuil = inputSeuil.value.trim() === '' ? 5 : Number(inputSeuil.value);
      preview.classList.remove('badge-ok', 'badge-low', 'badge-empty');

      if (!Number.isInteger(stock) || !Number.isInteger(seuil) || stock < 0 || seuil < 0) {
        preview.textContent = 'Valeurs invalides';
        return;
      }
      // Mêmes règles que EtatStock.calculer() en Java.
      if (stock === 0 || stock === seuil) {
        preview.textContent = 'Épuisé';
        preview.classList.add('badge-empty');
      } else if (stock < seuil) {
        preview.textContent = 'Faible';
        preview.classList.add('badge-low');
      } else {
        preview.textContent = 'Disponible';
        preview.classList.add('badge-ok');
      }
    };
    inputStock.addEventListener('input', actualiser);
    inputSeuil.addEventListener('input', actualiser);
    actualiser();
  }

  // Contrôle du fichier côté navigateur (5 Mo). Le serveur conserve sa validation.
  const tailleMax = 5 * 1024 * 1024;
  const extensions = /\.(csv|xlsx|xls|txt)$/i;
  document.querySelectorAll('form[data-validate-import], form.quick-import-form').forEach((form) => {
    const input = form.querySelector('input[type="file"]');
    if (!input) return;
    const message = form.querySelector('[data-file-message]');
    const valide = () => {
      const fichier = input.files && input.files[0];
      if (!fichier) return false;
      let erreur = '';
      if (!extensions.test(fichier.name)) erreur = 'Format non accepté : CSV, Excel ou TXT uniquement.';
      else if (fichier.size > tailleMax) erreur = 'Ce fichier dépasse 5 Mo.';
      if (message) {
        message.textContent = erreur || `Fichier sélectionné : ${fichier.name}`;
        message.classList.toggle('file-error', Boolean(erreur));
      }
      if (erreur) {
        if (!message) window.alert(erreur);
        input.value = '';
        return false;
      }
      return true;
    };
    input.addEventListener('change', () => {
      if (!valide()) return;
      if (input.hasAttribute('data-auto-import')) form.requestSubmit();
    });
    form.addEventListener('submit', (event) => {
      if (!valide()) event.preventDefault();
    });
  });
});
