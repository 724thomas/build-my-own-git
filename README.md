

# Git 클론 - Java 구현

[CodeCrafters](https://codecrafters.io/) Git Challenge 프로젝트로, Java를 사용하여 기본적인 Git 명령어들을 구현한 프로젝트입니다.

## 프로젝트 개요

이 프로젝트는 Git의 핵심 기능들을 Java로 직접 구현하여 Git의 내부 동작 원리를 이해하는 것을 목표로 합니다. Git 객체 모델, 압축, 해시 계산 등 Git의 핵심 개념들을 학습할 수 있습니다.

## 구현된 기능

### 기본 Git 명령어

- **`init`** - Git 저장소 초기화
- **`cat-file`** - Git 객체 내용 읽기 및 출력
- **`hash-object`** - 파일을 Git 블롭 객체로 변환하고 해시 생성
- **`ls-tree`** - 트리 객체의 내용 나열
- **`write-tree`** - 현재 디렉토리의 트리 객체 생성
- **`commit-tree`** - 커밋 객체 생성
- **`clone`** - 원격 저장소 복제

### 개발 과정

프로젝트는 다음과 같은 단계로 개발되었습니다:

1. **초기 설정** - 기본 프로젝트 구조 설정
2. **Blob 객체 읽기** - Git 객체 압축 해제 및 내용 출력
3. **Blob 객체 생성** - 파일을 Git 객체로 변환하고 저장
4. **Tree 객체 읽기** - 트리 객체 파싱 및 내용 나열
5. **Tree 객체 생성** - 디렉토리 구조를 트리 객체로 변환
6. **커밋 생성** - 커밋 메타데이터와 트리를 포함한 커밋 객체 생성
7. **저장소 복제** - JGit 라이브러리를 사용한 원격 저장소 복제

## 프로젝트 구조

```
src/main/java/
├── Main.java                    # 메인 엔트리포인트
├── command/                     # Git 명령어 구현
│   ├── GitCommand.java         # 명령어 인터페이스
│   ├── GitCommandFactory.java  # 명령어 팩토리
│   ├── InitCommand.java        # init 명령어
│   ├── CatFileCommand.java     # cat-file 명령어
│   ├── HashObjectCommand.java  # hash-object 명령어
│   ├── LsTreeCommand.java      # ls-tree 명령어
│   ├── WriteTreeCommand.java   # write-tree 명령어
│   ├── CommitTreeCommand.java  # commit-tree 명령어
│   └── CloneCommand.java       # clone 명령어
└── util/                       # 유틸리티 클래스
    ├── GitObjectUtil.java      # Git 객체 관련 유틸리티
    ├── HashUtil.java           # 해시 계산 유틸리티
    └── FileUtil.java           # 파일 처리 유틸리티
```

## 사용법

### 빌드

```bash
mvn compile
```

### 실행

```bash
# Git 저장소 초기화
java -cp target/classes Main init

# 파일을 Git 객체로 변환
java -cp target/classes Main hash-object -w <파일명>

# Git 객체 내용 출력
java -cp target/classes Main cat-file -p <객체 해시>

# 트리 객체 내용 나열
java -cp target/classes Main ls-tree --name-only <트리 해시>

# 현재 디렉토리의 트리 객체 생성
java -cp target/classes Main write-tree

# 커밋 생성
java -cp target/classes Main commit-tree <트리 해시> -m "<커밋 메시지>"

# 저장소 복제
java -cp target/classes Main clone <저장소 URL> <대상 디렉토리>
```

### 사용 예시

```bash
# Git 저장소 초기화
java -cp target/classes Main init

# 샘플 파일 생성
echo "Hello, Git!" > sample.txt

# 파일을 Git 객체로 변환
java -cp target/classes Main hash-object -w sample.txt

# 트리 객체 생성
java -cp target/classes Main write-tree

# 커밋 생성
java -cp target/classes Main commit-tree <트리해시> -m "Initial commit"
```

## 기술적 특징

### Git 객체 모델 구현
- **Blob 객체**: 파일 내용을 저장
- **Tree 객체**: 디렉토리 구조와 파일 메타데이터 저장
- **Commit 객체**: 커밋 정보와 트리 참조 저장

### 압축 및 해시
- **zlib 압축**: Git 객체의 압축/해제 구현
- **SHA-1 해시**: Git 객체 식별자 생성
- **16진수 변환**: 바이트 배열과 16진수 문자열 간 변환

### 객체 지향 설계
- **Command 패턴**: 각 Git 명령어를 독립적인 클래스로 구현
- **Factory 패턴**: 명령어 매핑 및 인스턴스 생성 관리
- **Utility 클래스**: 공통 기능의 재사용성 향상

## 의존성

- **Java 17+**: 최신 Java 기능 활용
- **JGit**: clone 명령어 구현을 위한 Git 라이브러리
- **Maven**: 빌드 및 의존성 관리

## 학습 포인트

이 프로젝트를 통해 다음을 학습할 수 있습니다:

1. **Git 내부 구조**: 객체 저장 방식, 해시 계산, 압축 메커니즘
2. **파일 시스템 조작**: Java NIO를 활용한 파일 읽기/쓰기
3. **압축 알고리즘**: zlib을 사용한 데이터 압축/해제
4. **객체 지향 설계**: Command 패턴과 Factory 패턴 활용
5. **Maven 빌드 시스템**: 의존성 관리와 빌드 자동화

## 라이선스

이 프로젝트는 교육 목적으로 만들어졌으며, CodeCrafters 플랫폼의 Git Challenge를 기반으로 합니다.
