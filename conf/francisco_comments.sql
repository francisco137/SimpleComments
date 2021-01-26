BEGIN TRANSACTION;

CREATE TABLE comments (
  id           serial  PRIMARY KEY,
  content      VARCHAR NOT NULL,
  content_dict VARCHAR,
  content_list VARCHAR
);

CREATE INDEX comments_idx_01 ON comments (content);
CREATE INDEX comments_idx_02 ON comments (content_dict);
CREATE INDEX comments_idx_03 ON comments (content_list);
------------------------------------------------------------------------------------
CREATE OR REPLACE FUNCTION commentDict(content varchar)
  RETURNS varchar AS
$BODY$
declare
  t_content varchar;
  r_content varchar;
begin
  t_content = LOWER(regexp_replace(content,'[^\w]+',' ','g'));
  t_content = replace(t_content,'"',' ');
  SELECT array_to_string(array_agg(word),' ',' ') INTO r_content FROM
    (SELECT word FROM
       (SELECT unnest(word) AS word FROM (SELECT regexp_split_to_array(LOWER(t_content),'\s+') as word) as f) as g
     WHERE trim(word)<>'' GROUP BY word ORDER BY word) as h;

  return r_content;
end;
$BODY$
  LANGUAGE plpgsql VOLATILE
;
------------------------------------------------------------------------------------
    CREATE OR REPLACE FUNCTION commentList(content varchar)
      RETURNS varchar AS
    $BODY$
    declare
      t_content varchar;
      r_content varchar;
    begin
      t_content = LOWER(regexp_replace(content,'[^\w]+',' ','g'));
      SELECT array_to_string(array_agg(word),' ',' ') INTO r_content FROM
        (SELECT word FROM
           (SELECT unnest(word) AS word FROM (SELECT regexp_split_to_array(LOWER(t_content),'\s+') as word) as f) as g
         WHERE trim(word)<>'') as h;
      return r_content;
    end;
    $BODY$
      LANGUAGE plpgsql VOLATILE
    ;
------------------------------------------------------------------------------------
CREATE OR REPLACE FUNCTION public.comments()
  RETURNS trigger AS
$BODY$
begin
  if TG_OP in ('INSERT','UPDATE') then
    new.content_dict = commentDict(new.content);
    new.content_list = commentList(new.content);
  end if;

  return new;
end;
$BODY$
  LANGUAGE plpgsql VOLATILE
;

------------------------------------------------------------------------------------

--  DROP TRIGGER comments ON public.comments;
CREATE TRIGGER comments BEFORE INSERT OR UPDATE ON public.comments
  FOR EACH ROW EXECUTE PROCEDURE public.comments();

COMMIT;
