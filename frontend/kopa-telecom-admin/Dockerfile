# node:*-alpine (musl libc) won't load this app's native deps - package-lock.json only carries
# the glibc build of sharp/lightningcss (sharp-linux-x64, lightningcss-linux-x64-gnu, no -musl
# counterparts) - so every stage here uses the Debian-based "slim" variant instead.
FROM node:20-bookworm-slim AS deps
WORKDIR /app
COPY package.json package-lock.json ./
RUN npm ci

FROM node:20-bookworm-slim AS build
WORKDIR /app
COPY --from=deps /app/node_modules ./node_modules
COPY . .
RUN npm run build

# Runtime image only needs the standalone server Next traced for us (next.config.ts's
# output: "standalone") - not the full node_modules tree from the build stage.
FROM node:20-bookworm-slim AS runtime
WORKDIR /app
ENV NODE_ENV=production
COPY --from=build /app/public ./public
COPY --from=build /app/.next/standalone ./
COPY --from=build /app/.next/static ./.next/static
EXPOSE 3000
ENV PORT=3000
CMD ["node", "server.js"]
