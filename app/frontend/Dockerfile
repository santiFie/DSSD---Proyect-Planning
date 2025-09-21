# Etapa de build
FROM node:20 AS build
WORKDIR /app

# Copiar archivos de configuración
COPY package.json ./
COPY package-lock.json* ./

# Instalar dependencias
RUN npm install

# Copiar el código fuente
COPY . .

# Construir la aplicación
RUN npm run build:prod

# Etapa de producción con Nginx
FROM nginx:alpine
COPY --from=build /app/dist/proyect-planning-frontend /usr/share/nginx/html

# Configurar nginx para Angular routing
RUN echo 'server { \
    listen 80; \
    server_name localhost; \
    root /usr/share/nginx/html; \
    index index.html; \
    location / { \
        try_files $uri $uri/ /index.html; \
    } \
}' > /etc/nginx/conf.d/default.conf

EXPOSE 80
