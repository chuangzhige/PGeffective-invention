<template>
  <Card title="考勤状态统计" :loading="loading">
    <div ref="chartRef" :style="{ width, height }"></div>
  </Card>
</template>

<script lang="ts" setup>
import { Ref, ref, watch } from "vue";
import { Card } from "ant-design-vue";
import { useECharts } from "@/hooks/web/useECharts";

const props = defineProps({
  loading: Boolean,
  width: {
    type: String as PropType<string>,
    default: "100%",
  },
  height: {
    type: String as PropType<string>,
    default: "300px",
  },
});

const chartRef = ref<HTMLDivElement | null>(null);
const { setOptions } = useECharts(chartRef as Ref<HTMLDivElement>);

watch(
  () => props.loading,
  () => {
    if (props.loading) {
      return;
    }
    setOptions({
      tooltip: {
        trigger: "item",
      },
      legend: {
        orient: "vertical",
        right: 10,
        top: "center",
      },
      series: [
        {
          name: "考勤状态",
          type: "pie",
          radius: ["40%", "70%"],
          center: ["40%", "50%"],
          color: ["#52c41a", "#faad14", "#ff4d4f", "#1890ff", "#d9d9d9"],
          data: [
            { value: 150, name: "正常" },
            { value: 30, name: "迟到" },
            { value: 10, name: "缺勤" },
            { value: 20, name: "请假" },
            { value: 5, name: "其他" },
          ],
          label: {
            show: true,
            formatter: "{b}: {c} ({d}%)",
          },
          emphasis: {
            itemStyle: {
              shadowBlur: 10,
              shadowOffsetX: 0,
              shadowColor: "rgba(0, 0, 0, 0.5)",
            },
          },
        },
      ],
    });
  },
  { immediate: true },
);
</script>