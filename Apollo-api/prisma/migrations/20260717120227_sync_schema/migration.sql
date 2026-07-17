/*
  Warnings:

  - You are about to drop the column `entity_id` on the `reports` table. All the data in the column will be lost.
  - You are about to drop the column `user_id` on the `reports` table. All the data in the column will be lost.
  - Added the required column `us er_id` to the `reports` table without a default value. This is not possible if the table is not empty.

*/
-- DropForeignKey
ALTER TABLE "entity_relations" DROP CONSTRAINT "entity_relations_source_id_fkey";

-- DropForeignKey
ALTER TABLE "entity_relations" DROP CONSTRAINT "entity_relations_target_id_fkey";

-- DropForeignKey
ALTER TABLE "reports" DROP CONSTRAINT "reports_entity_id_fkey";

-- DropForeignKey
ALTER TABLE "reports" DROP CONSTRAINT "reports_user_id_fkey";

-- DropIndex
DROP INDEX "reports_entity_id_idx";

-- DropIndex
DROP INDEX "reports_user_id_idx";

-- AlterTable
ALTER TABLE "entities" ADD COLUMN     "category" TEXT;

-- AlterTable
ALTER TABLE "reports" DROP COLUMN "entity_id",
DROP COLUMN "user_id",
ADD COLUMN     "us er_id" INTEGER NOT NULL;

-- CreateTable
CREATE TABLE "report_entities" (
    "id" SERIAL NOT NULL,
    "report_id" INTEGER NOT NULL,
    "entity_id" INTEGER NOT NULL,

    CONSTRAINT "report_entities_pkey" PRIMARY KEY ("id")
);

-- CreateIndex
CREATE INDEX "report_entities_report_id_idx" ON "report_entities"("report_id");

-- CreateIndex
CREATE INDEX "report_entities_entity_id_idx" ON "report_entities"("entity_id");

-- CreateIndex
CREATE UNIQUE INDEX "report_entities_report_id_entity_id_key" ON "report_entities"("report_id", "entity_id");

-- CreateIndex
CREATE INDEX "reports_us er_id_idx" ON "reports"("us er_id");

-- AddForeignKey
ALTER TABLE "reports" ADD CONSTRAINT "reports_us er_id_fkey" FOREIGN KEY ("us er_id") REFERENCES "users"("id") ON DELETE CASCADE ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "report_entities" ADD CONSTRAINT "report_entities_report_id_fkey" FOREIGN KEY ("report_id") REFERENCES "reports"("id") ON DELETE CASCADE ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "report_entities" ADD CONSTRAINT "report_entities_entity_id_fkey" FOREIGN KEY ("entity_id") REFERENCES "entities"("id") ON DELETE CASCADE ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "entity_relations" ADD CONSTRAINT "entity_relations_source_id_fkey" FOREIGN KEY ("source_id") REFERENCES "entities"("id") ON DELETE CASCADE ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "entity_relations" ADD CONSTRAINT "entity_relations_target_id_fkey" FOREIGN KEY ("target_id") REFERENCES "entities"("id") ON DELETE CASCADE ON UPDATE CASCADE;
