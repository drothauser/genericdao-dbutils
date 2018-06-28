--  -------------------------------------------------- 
--  Generated by Enterprise Architect Version 11.1.1112
--  Created On : Tuesday, 16 June, 2015 
--  DBMS       : MySql 
--  -------------------------------------------------- 

DROP VIEW PRESIDENTS_VIEW CASCADE
;
CREATE VIEW PRESIDENTS_VIEW AS
SELECT a.lastname, a.firstname, a.inaugurated_year, a. years, b.name AS "STATE", c.name AS "PARTY"
FROM PRESIDENT a
JOIN STATE b
ON a.state_id = b.id
JOIN PARTY c
ON a.party_id = c.id
;

