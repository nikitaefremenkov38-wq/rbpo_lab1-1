INSERT INTO attractions (name, description, capacity)
VALUES
    ('Roller Coaster', 'Fast steel coaster with two loops', 24),
    ('Ferris Wheel', 'Panoramic view over the whole park', 40),
    ('Haunted Mansion', 'Indoor dark ride with actors', 18)
ON CONFLICT (name) DO NOTHING;

INSERT INTO visitors (full_name, email, active)
VALUES
    ('Anna Petrova', 'anna.petrova@example.com', true),
    ('Ivan Sidorov', 'ivan.sidorov@example.com', true),
    ('Olga Ivanova', 'olga.ivanova@example.com', true)
ON CONFLICT (email) DO NOTHING;

INSERT INTO schedules (attraction_id, start_time, end_time)
SELECT a.id, t.start_time, t.end_time
FROM attractions a
JOIN (
    VALUES
        ('Roller Coaster', TIMESTAMP '2026-03-07 10:00:00', TIMESTAMP '2026-03-07 10:20:00'),
        ('Roller Coaster', TIMESTAMP '2026-03-07 11:00:00', TIMESTAMP '2026-03-07 11:20:00'),
        ('Ferris Wheel', TIMESTAMP '2026-03-07 10:00:00', TIMESTAMP '2026-03-07 10:30:00'),
        ('Haunted Mansion', TIMESTAMP '2026-03-07 12:00:00', TIMESTAMP '2026-03-07 12:30:00')
) AS t(attraction_name, start_time, end_time) ON t.attraction_name = a.name
ON CONFLICT (attraction_id, start_time, end_time) DO NOTHING;

INSERT INTO maintenances (attraction_id, start_time, end_time, reason)
SELECT a.id, TIMESTAMP '2026-03-08 09:00:00', TIMESTAMP '2026-03-08 11:00:00', 'Safety diagnostics'
FROM attractions a
WHERE a.name = 'Haunted Mansion'
ON CONFLICT (attraction_id, start_time, end_time) DO NOTHING;

INSERT INTO tickets (visitor_id, schedule_id, created_at)
SELECT v.id, s.id, TIMESTAMP '2026-03-06 09:00:00'
FROM visitors v
JOIN schedules s ON s.start_time = TIMESTAMP '2026-03-07 10:00:00'
JOIN attractions a ON a.id = s.attraction_id
WHERE v.email = 'anna.petrova@example.com' AND a.name = 'Roller Coaster'
ON CONFLICT (visitor_id, schedule_id) DO NOTHING;
