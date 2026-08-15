<script setup>
import AppHeader from './components/AppHeader.vue'
</script>

<template>
  <div class="app-layout">
    <AppHeader />
    <main class="main-content">
      <router-view />
    </main>
  </div>
</template>
