const copyButton = document.getElementById("copyButton");

copyButton.addEventListener("click", event => {
    const codeField = document.getElementById("code");
    const copyPromise = navigator.clipboard.writeText(codeField.textContent)
    copyPromise
        .then(() => alert("Скопировал код: " + codeField.textContent))
        .catch((reason) => alert("Ошибка при копировании кода"))
})
