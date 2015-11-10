select count(id) as num, year(in_date_time) as year
from vehicle_parking
group by year(in_date_time)
order by in_date_time asc

select 
sum(month(in_date_time) = 01) as jan,
sum(month(in_date_time) = 02) as feb,
sum(month(in_date_time) = 03) as mar,
sum(month(in_date_time) = 04) as apr,
sum(month(in_date_time) = 05) as may,
sum(month(in_date_time) = 06) as june,
sum(month(in_date_time) = 07) as july,
sum(month(in_date_time) = 08) as aug,
sum(month(in_date_time) = 09) as sept,
sum(month(in_date_time) = 10) as oct,
sum(month(in_date_time) = 11) as nov,
sum(month(in_date_time) = 12) as decem

from vehicle_parking
where year(in_date_time) = 2013

//------------------------------: Money :---------------------------------------
select 
sum(case when month(in_date_time) = 01 THEN price ELSE 0 END) as jan,
sum(case when month(in_date_time) = 02 THEN price ELSE 0 END) as feb,
sum(case when month(in_date_time) = 03 THEN price ELSE 0 END) as mar,
sum(case when month(in_date_time) = 04 THEN price ELSE 0 END) as apr,
sum(case when month(in_date_time) = 05 THEN price ELSE 0 END) as may,
sum(case when month(in_date_time) = 06 THEN price ELSE 0 END) as june,
sum(case when month(in_date_time) = 07 THEN price ELSE 0 END) as july,
sum(case when month(in_date_time) = 08 THEN price ELSE 0 END) as aug,
sum(case when month(in_date_time) = 09 THEN price ELSE 0 END) as sept,
sum(case when month(in_date_time) = 10 THEN price ELSE 0 END) as oct,
sum(case when month(in_date_time) = 11 THEN price ELSE 0 END) as nov,
sum(case when month(in_date_time) = 12 THEN price ELSE 0 END) as decem

from vehicle_parking
where year(in_date_time) = 2014


//---------------------------------------------------------------------

select 
sum(day(in_date_time) = 01) as i,
sum(day(in_date_time) = 02) as ii,
sum(day(in_date_time) = 03) as iii,
sum(day(in_date_time) = 04) as iv,
sum(day(in_date_time) = 05) as v,
sum(day(in_date_time) = 06) as vi,
sum(day(in_date_time) = 07) as vii,
sum(day(in_date_time) = 08) as viii,
sum(day(in_date_time) = 09) as ix,
sum(day(in_date_time) = 10) as x,
sum(day(in_date_time) = 11) as xi,
sum(day(in_date_time) = 12) as xii,
sum(day(in_date_time) = 13) as xiii,
sum(day(in_date_time) = 14) as xiv,
sum(day(in_date_time) = 15) as xv,
sum(day(in_date_time) = 16) as xvi,
sum(day(in_date_time) = 17) as xvii,
sum(day(in_date_time) = 18) as xviii,
sum(day(in_date_time) = 19) as xix,
sum(day(in_date_time) = 20) as xx,
sum(day(in_date_time) = 21) as xxi,
sum(day(in_date_time) = 22) as xxii,
sum(day(in_date_time) = 23) as xxiii,
sum(day(in_date_time) = 24) as xxiv,
sum(day(in_date_time) = 25) as xxv,
sum(day(in_date_time) = 26) as xxvi,
sum(day(in_date_time) = 27) as xxvii,
sum(day(in_date_time) = 28) as xxviii,
sum(day(in_date_time) = 29) as xxix,
sum(day(in_date_time) = 30) as xxx,
sum(day(in_date_time) = 31) as xxxi

from vehicle_parking
where year(in_date_time) = 2013 and month(in_date_time) = 01