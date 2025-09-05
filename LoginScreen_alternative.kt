// 만약 아이콘이 여전히 작동하지 않는다면 이 방법을 사용하세요:

trailingIcon = {
    TextButton(onClick = { passwordVisible = !passwordVisible }) {
        Text(
            text = if (passwordVisible) "숨기기" else "보기",
            style = MaterialTheme.typography.bodySmall
        )
    }
}
