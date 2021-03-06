#
# Transofrm raw events data as generated by EventDataPopulator to
# a CSV file of conversion rates consumable by Excel or Tableau.
#

export PGPASSWORD=variant
psql -U variant <<EOF 
  COPY (
    WITH t AS
	  (
		SELECT
		  seconds.sec date,
		  sessions.*
		from
		  (
			SELECT
			  distinct session_id, test_name, experience_name,
			  (select min(created_on) from events_v where session_id = x.session_id) created_on,
			  EXISTS (SELECT FROM events_v where session_id = x.session_id and event_value = 'ownerDetail') converted
			FROM events_v x
			WHERE EXISTS (SELECT FROM events_v where session_id = x.session_id and event_value = 'newOwner')
		  ) sessions,
		  (
		    select distinct date_trunc('second', created_on) sec
		    from events_v
		  ) seconds
		WHERE
			seconds.sec = date_trunc('second', sessions.created_on)
	  )
	SELECT DISTINCT
	  date,
	  test_name,
	  experience_name,
	  sum(CASE WHEN converted THEN 1 ELSE 0 END) OVER (PARTITION BY experience_name ORDER BY date) ::FLOAT / count(*) OVER (PARTITION BY experience_name ORDER BY date) cume_conv_rate,
	  count(*) OVER (PARTITION BY experience_name ORDER BY date) cume_total
	FROM t
	ORDER BY date, experience_name    
  ) to STDOUT CSV HEADER FORCE QUOTE *
EOF