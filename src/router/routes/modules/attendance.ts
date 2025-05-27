import type { AppRouteModule } from "@/router/types";

import { LAYOUT } from "@/router/constant";
import { t } from "@/hooks/web/useI18n";

const attendance: AppRouteModule = {
  path: "/attendance",
  name: "Attendance",
  component: LAYOUT,
  redirect: "/attendance/dashboard",
  meta: {
    orderNo: 20,
    icon: "ion:calendar-outline",
    title: "考勤管理",
  },
  children: [
    {
      path: "dashboard",
      name: "AttendanceDashboard",
      component: () => import("@/views/attendance/dashboard/index.vue"),
      meta: {
        title: "考勤统计",
      },
    },
    {
      path: "personal-management",
      name: "PersonalAttendanceManagement",
      component: () => import("@/views/attendance/personal-management/index.vue"),
      meta: {
        title: "个人考勤管理",
      },
    },
    {
      path: "personal/:id",
      name: "PersonalAttendance",
      component: () => import("@/views/attendance/personal/index.vue"),
      meta: {
        title: "个人统计",
        hideMenu: true,
      },
    },
  ],
};

export default attendance;