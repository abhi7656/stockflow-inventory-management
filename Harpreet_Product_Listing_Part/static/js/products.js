// Update API paths and JSON field names to match Abhinass's backend.
const PRODUCTS_API = "/api/products";
const FILTERS_API = "/api/products/filters";

const state = {
  page: 0,
  size: 5,
  search: "",
  brand: "",
  category: "",
  sortBy: "name",
  direction: "asc"
};

const body = document.getElementById("productsTableBody");
const pagination = document.getElementById("pagination");
const errorBox = document.getElementById("errorMessage");
const resultCount = document.getElementById("resultCount");

function showError(message) {
  errorBox.textContent = message;
  errorBox.classList.remove("d-none");
}
function clearError() {
  errorBox.textContent = "";
  errorBox.classList.add("d-none");
}
function escapeHtml(value) {
  return String(value ?? "").replace(/[&<>"']/g, ch => ({
    "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;"
  }[ch]));
}

async function loadFilters() {
  try {
    const response = await fetch(FILTERS_API);
    if (!response.ok) return; // Filter endpoint is optional until backend confirms it.
    const data = await response.json();
    addOptions("brandFilter", data.brands || [], "All brands");
    addOptions("categoryFilter", data.categories || [], "All categories");
  } catch (error) {
    // The listing remains usable if the optional filters endpoint is not ready.
  }
}
function addOptions(elementId, values, firstLabel) {
  const select = document.getElementById(elementId);
  const current = select.value;
  select.innerHTML = `<option value="">${firstLabel}</option>`;
  [...new Set(values)].forEach(value => {
    const option = document.createElement("option");
    option.value = value;
    option.textContent = value;
    select.appendChild(option);
  });
  select.value = current;
}

async function loadProducts() {
  clearError();
  body.innerHTML = '<tr><td colspan="5" class="text-center py-4">Loading products...</td></tr>';

  const params = new URLSearchParams({
    page: state.page,
    size: state.size,
    search: state.search,
    brand: state.brand,
    category: state.category,
    sortBy: state.sortBy,
    direction: state.direction
  });

  try {
    const response = await fetch(`${PRODUCTS_API}?${params.toString()}`);
    if (!response.ok) throw new Error(`Server returned ${response.status}`);
    const data = await response.json();

    // Expected Spring Page-style response: {content, totalPages, number, totalElements}
    const products = Array.isArray(data) ? data : (data.content || data.products || []);
    if (products.length === 0) {
      body.innerHTML = '<tr><td colspan="5" class="text-center text-secondary py-4">No products found.</td></tr>';
    } else {
      body.innerHTML = products.map(product => `
        <tr>
          <td>${escapeHtml(product.id)}</td>
          <td>${escapeHtml(product.name ?? product.productName)}</td>
          <td>${escapeHtml(product.brand)}</td>
          <td>${escapeHtml(product.category)}</td>
          <td class="text-end">${Number(product.price ?? 0).toLocaleString(undefined, {
            style: "currency", currency: "CAD"
          })}</td>
        </tr>
      `).join("");
    }

    const totalPages = data.totalPages ?? 1;
    const totalElements = data.totalElements ?? products.length;
    const currentPage = data.number ?? state.page;
    resultCount.textContent = `${totalElements} product(s) found`;
    renderPagination(currentPage, totalPages);
  } catch (error) {
    body.innerHTML = '<tr><td colspan="5" class="text-center text-secondary py-4">Could not load products.</td></tr>';
    resultCount.textContent = "Products could not be loaded";
    pagination.innerHTML = "";
    showError("Could not connect to the products API. Check that the Spring Boot app is running and confirm the API URL and response format with Abhinass.");
  }
}

function renderPagination(currentPage, totalPages) {
  pagination.innerHTML = "";
  if (totalPages <= 1) return;

  const addButton = (label, page, disabled, active = false) => {
    const li = document.createElement("li");
    li.className = `page-item${disabled ? " disabled" : ""}${active ? " active" : ""}`;
    const button = document.createElement("button");
    button.className = "page-link";
    button.type = "button";
    button.textContent = label;
    button.disabled = disabled;
    button.addEventListener("click", () => {
      state.page = page;
      loadProducts();
    });
    li.appendChild(button);
    pagination.appendChild(li);
  };

  addButton("Previous", currentPage - 1, currentPage <= 0);
  for (let i = 0; i < totalPages; i++) addButton(String(i + 1), i, false, i === currentPage);
  addButton("Next", currentPage + 1, currentPage >= totalPages - 1);
}

document.getElementById("searchInput").addEventListener("input", event => {
  state.search = event.target.value.trim();
  state.page = 0;
  loadProducts();
});
document.getElementById("brandFilter").addEventListener("change", event => {
  state.brand = event.target.value;
  state.page = 0;
  loadProducts();
});
document.getElementById("categoryFilter").addEventListener("change", event => {
  state.category = event.target.value;
  state.page = 0;
  loadProducts();
});
document.getElementById("sortSelect").addEventListener("change", event => {
  [state.sortBy, state.direction] = event.target.value.split(":");
  state.page = 0;
  loadProducts();
});
document.getElementById("pageSize").addEventListener("change", event => {
  state.size = Number(event.target.value);
  state.page = 0;
  loadProducts();
});
document.getElementById("clearFilters").addEventListener("click", () => {
  state.search = "";
  state.brand = "";
  state.category = "";
  state.sortBy = "name";
  state.direction = "asc";
  state.page = 0;
  document.getElementById("searchInput").value = "";
  document.getElementById("brandFilter").value = "";
  document.getElementById("categoryFilter").value = "";
  document.getElementById("sortSelect").value = "name:asc";
  loadProducts();
});

loadFilters().finally(loadProducts);
