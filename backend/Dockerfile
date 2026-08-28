# Production Dockerfile for RailSaathi on Google Cloud Run / Google Cloud
FROM node:20-alpine AS builder

WORKDIR /app

# Copy dependency definitions
COPY package*.json tsconfig.json ./

# Install dependencies
RUN npm install

# Copy source code and api handlers
COPY src ./src
COPY api ./api

# Build TypeScript to JavaScript in /app/dist
RUN npm run build

# Production image
FROM node:20-alpine AS runner

WORKDIR /app

ENV NODE_ENV=production
ENV PORT=8080

COPY package*.json ./
RUN npm install --omit=dev

# Copy compiled JavaScript from builder
COPY --from=builder /app/dist ./dist

# Non-root user for security
USER node

EXPOSE 8080

CMD ["node", "dist/src/server.js"]
