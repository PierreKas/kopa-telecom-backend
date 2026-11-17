import type { NextConfig } from "next";

const nextConfig: NextConfig = {
  // Lets the Docker image copy just .next/standalone + .next/static + public, instead of the
  // full node_modules tree - see frontend/kopa-telecom-admin/Dockerfile.
  output: "standalone",
  experimental: {
    agentFeedback: true,
  },
  cacheComponents: true,
  partialPrefetching: true,
  turbopack: {
    rules: {
      "*.css": {
        loaders: ["@tailwindcss/turbopack"],
        as: "*.css",
      },
    },
  },
};

export default nextConfig;
