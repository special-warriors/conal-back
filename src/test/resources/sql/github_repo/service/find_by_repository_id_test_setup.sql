insert into users(id, github_id, username, avatar_url)
values (1, 1, 'username', 'avatar_url');

insert into github_repos(id, name, url, end_date, user_id)
values (1, 'repo', 'repo_url', now(), 1);