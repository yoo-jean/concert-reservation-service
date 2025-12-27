insert into concert_date(concert_id, date) values (1, '2026-01-10');
insert into concert_date(concert_id, date) values (1, '2026-01-11');

-- dateId는 1,2로 생성된다고 가정 (H2 create-drop 환경에서 보통 순서대로)
-- 1번 날짜 좌석 1~50
insert into seat(concert_date_id, seat_no, price, status, hold_by, hold_until, owner_user_id, version)
select 1, x, 8000, 'AVAILABLE', null, null, null, 0
from system_range(1, 50);

-- 2번 날짜 좌석 1~50
insert into seat(concert_date_id, seat_no, price, status, hold_by, hold_until, owner_user_id, version)
select 2, x, 8000, 'AVAILABLE', null, null, null, 0
from system_range(1, 50);

-- 테스트용 지갑
insert into point_wallet(user_id, balance) values ('u1', 100000);
insert into point_wallet(user_id, balance) values ('u2', 100000);
