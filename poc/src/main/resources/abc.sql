CREATE TABLE "user_account"
(
    "id"             INT GENERATED ALWAYS AS IDENTITY
        CONSTRAINT "user_account_pk" PRIMARY KEY,
    "wallet_address" TEXT        NOT NULL,
    "discord_id"     TEXT,
    "telegram_id"    TEXT,
    "last_login"     TIMESTAMPTZ NOT NULL,
    "created_at"     TIMESTAMPTZ NOT NULL DEFAULT now(),
    "updated_at"     TIMESTAMPTZ NOT NULL,
    "test_field_1"   BOOLEAN     NOT NULL,
    "test_field_2"   uuid        NOT NULL,
    "test_field_3"   decimal     NOT NULL,
    "test_feild_4"   bigint      NOT NULL
);

CREATE UNIQUE INDEX "user_account_wallet_address_uindex" ON "user_account" ("wallet_address");
