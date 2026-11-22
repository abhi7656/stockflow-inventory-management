HARPREET KAUR - PRODUCT LISTING & SEARCH PART

This folder contains the starter files for your assigned part:
- Product listing page
- Brand and category filters
- Search by product name
- Sort by product name or price
- Pagination
- Display products returned by your team's backend

FILES
1. products.html
2. static/css/products.css
3. static/js/products.js

HOW TO ADD TO THE TEAM'S SPRING BOOT PROJECT
Copy products.html into:
  src/main/resources/templates/products.html

Copy products.css into:
  src/main/resources/static/css/products.css

Copy products.js into:
  src/main/resources/static/js/products.js

IMPORTANT BACKEND CONNECTION
The JavaScript currently expects a JSON API at:
  GET /api/products?page=0&size=5&search=&brand=&category=&sortBy=name&direction=asc

Expected JSON response:
{
  "content": [
    {
      "id": 1,
      "name": "Wireless Mouse",
      "brand": "Logitech",
      "category": "Accessories",
      "price": 24.99
    }
  ],
  "totalPages": 2,
  "number": 0,
  "totalElements": 8
}

This is a suggested API contract, NOT confirmed from your teammate's message.
Ask Abhinass for the actual API URL, JSON field names, and pagination response format.
If his API uses different names or a different response structure, products.js must be adjusted.

The page also expects a GET /api/products/filters endpoint returning:
{
  "brands": ["Logitech", "Samsung"],
  "categories": ["Accessories", "Electronics"]
}
If this endpoint is not available, the page still works with manually entered filter values, but ask your teammate to provide filter options or add them to the API.

Do not overwrite your teammates' files. Copy only these three files into the matching locations after checking the existing project structure.
