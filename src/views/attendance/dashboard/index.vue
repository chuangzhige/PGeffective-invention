<template>
  <div class="p-4">
    <div class="md:flex enter-y">
      <AttendancePie class="md:w-1/2 w-full !md:mr-4 !md:my-0 !my-4" :loading="loading" />
      <AttendanceList class="md:w-1/2 w-full" :loading="loading" />
    </div>
  </div>
</template>

<script lang="ts" setup>
import { ref } from "vue";
import AttendancePie from "./components/AttendancePie.vue";
import AttendanceList from "./components/AttendanceList.vue";

const loading = ref(true);

setTimeout(() => {
  loading.value = false;
}, 500);
</script>