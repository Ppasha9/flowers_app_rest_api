FROM python:3.7-alpine
MAINTAINER Pavel Denisov

ENV PYTHONUNBUFFERED 1

EXPOSE 8000

COPY ./requirements.txt /requirements.txt
RUN apk add --update --no-cache postgresql-client
RUN apk add --update --no-cache --virtual \
    .tmp-build-deps \
    gcc libc-dev \
    linux-headers \
    postgresql-dev \
    libressl-dev \
    musl-dev \
    libffi-dev
RUN pip install --no-cache-dir -r /requirements.txt
RUN apk del .tmp-build-deps

RUN mkdir /app
WORKDIR /app
COPY ./app /app

RUN adduser -D user
USER user
