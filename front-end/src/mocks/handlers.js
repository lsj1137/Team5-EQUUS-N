import { http, HttpResponse } from 'msw';
import { teams, schedules, members, notifications } from './mockData';

const BASE_URL = 'https://api.com';

export const handlers = [
  // 팀 목록 조회
  http.get(`${BASE_URL}/api/team/my-teams`, () => {
    return HttpResponse.json(teams);
  }),

  // 메인 카드 조회
  http.get(`${BASE_URL}/recentSchedule/:teamId`, () => {
    return HttpResponse.json(schedules[0]);
  }),

  // 팀 멤버 조회
  http.get(`${BASE_URL}/api/team/:teamId/members`, () => {
    return HttpResponse.json(members);
  }),

  // 알람 조회
  http.get(`${BASE_URL}/api/notification`, () => {
    return HttpResponse.json(notifications);
  }),

  // 알람 읽음 처리
  http.post(
    `${BASE_URL}/api/notification/mark-as-read`,
    async ({ request }) => {
      const responseData = await request.json();
      console.log('서버가 잘 받았따리: ', responseData);
      return HttpResponse.json(responseData, { status: 201 });
    },
  ),

  // 피드백 요청
  http.post(
    `${BASE_URL}/api/feedbacks/frequent/request`,
    async ({ request }) => {
      const data = await request.json();
      console.log(data);

      return HttpResponse.json(data, { status: 201 });
    },
  ),
];
