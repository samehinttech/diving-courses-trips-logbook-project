FROM ubuntu:latest
LABEL authors="sameh"

ENTRYPOINT ["top", "-b"]