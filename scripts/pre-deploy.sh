#!/usr/bin/env bash

ARTIFACT_DIR="target"

GPG_OPTS="--armor --detach-sign --batch --yes"

GROUP_ID="io.github.grano22"
ARTIFACT_ID="image-utils-webp"
VERSION="1.0.0"

GROUP_PATH=$(echo "$GROUP_ID" | tr '.' '/')

PATTERNS=("*.jar" "*.pom")

PACKAGE_DIR="./package/$GROUP_PATH/$ARTIFACT_ID/$VERSION/"
ZIP_NAME="${ARTIFACT_ID}-${VERSION}.zip"

echo "📦 Signing and generating checksums in $ARTIFACT_DIR..."
mkdir -p "$PACKAGE_DIR"

for pattern in "${PATTERNS[@]}"; do
  for file in "$ARTIFACT_DIR"/$pattern; do
    [ -e "$file" ] || continue

    echo "Processing: $file"

    gpg $GPG_OPTS "$file"

    sha1sum "$file" | awk '{print $1}' > "$file.sha1"

    md5sum "$file" | awk '{print $1}' > "$file.md5"

    cp "$file" "$PACKAGE_DIR"

    base=$(basename "$file")

    cp "$file" "$PACKAGE_DIR"
    for ext in asc sha1 md5; do
        [ -e "$ARTIFACT_DIR/$base.$ext" ] && cp "$ARTIFACT_DIR/$base.$ext" "$PACKAGE_DIR"
      done
  done
done

cd package
echo "🗜️ Creating ZIP: $ZIP_NAME"
zip -r "$ZIP_NAME" "./$GROUP_PATH/$ARTIFACT_ID/$VERSION/"
cd ..

echo "✅ All files signed and checksums generated."