<template>
  <router-view v-slot="{ Component, route }">
    <transition name="route-fade-slide" mode="out-in">
      <component :is="Component" :key="route.fullPath" />
    </transition>
  </router-view>
</template>
