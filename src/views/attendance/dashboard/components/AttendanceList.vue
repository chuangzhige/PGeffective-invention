<template>
  <Card title="考勤记录" :loading="loading">
    <Table :columns="columns" :data-source="dataSource" :pagination="false">
      <template #bodyCell="{ column, text, record }">
        <template v-if="column.key === 'status'">
          <Tag :color="getStatusColor(text)">{{ text }}</Tag>
        </template>
        <template v-if="column.key === 'userName'">
          <a @click="showPersonalAttendance(record as DataItem)">{{ text }}</a>
        </template>
      </template>
    </Table>
  </Card>
</template>

<script lang="ts" setup>
import { ref } from "vue";
import { Card, Table, Tag } from "ant-design-vue";
import type { TableColumnsType } from "ant-design-vue";
import { useRouter } from "vue-router";

const props = defineProps({
  loading: Boolean,
});

const router = useRouter();

interface DataItem {
  key: string;
  userId: string;
  userName: string;
  checkTime: string;
  location: string;
  type: string;
  status: string;
}

const showPersonalAttendance = (record: DataItem) => {
  router.push(`/attendance/personal-management?userId=${record.userId}`);
};

const getStatusColor = (status: string) => {
  const colorMap = {
    正常: "success",
    迟到: "warning",
    缺勤: "error",
    请假: "processing",
    其他: "default",
  };
  return colorMap[status] || "default";
};

const columns: TableColumnsType = [
  {
    title: "用户ID",
    dataIndex: "userId",
    key: "userId",
    width: 100,
  },
  {
    title: "姓名",
    dataIndex: "userName",
    key: "userName",
    width: 100,
  },
  {
    title: "打卡时间",
    dataIndex: "checkTime",
    key: "checkTime",
    width: 150,
  },
  {
    title: "打卡地点",
    dataIndex: "location",
    key: "location",
    width: 150,
  },
  {
    title: "打卡类型",
    dataIndex: "type",
    key: "type",
    width: 100,
  },
  {
    title: "状态",
    dataIndex: "status",
    key: "status",
    width: 100,
  },
];

const dataSource = ref<DataItem[]>([
  {
    key: "1",
    userId: "abd754f7",
    userName: "张三",
    checkTime: "2024-01-24 09:00:00",
    location: "西溪八方城",
    type: "上班打卡",
    status: "正常",
  },
  {
    key: "2",
    userId: "abd754f8",
    userName: "李四",
    checkTime: "2024-01-24 09:15:00",
    location: "西溪八方城",
    type: "上班打卡",
    status: "迟到",
  },
  {
    key: "3",
    userId: "abd754f9",
    userName: "王五",
    checkTime: "2024-01-24 18:00:00",
    location: "西溪八方城",
    type: "下班打卡",
    status: "正常",
  },
]);
</script>