update itembatch
set `DATEOFEXPIRE` = DATE(DATE_ADD(`DATEOFEXPIRE`, INTERVAL 2000 YEAR))
WHERE  `DATEOFEXPIRE` <  '20000401' ;