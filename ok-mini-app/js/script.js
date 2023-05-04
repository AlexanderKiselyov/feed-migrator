const params = new Proxy(new URLSearchParams(window.location.search), {
    get: (searchParams, prop) => searchParams.get(prop)
})

const custom_args = new Proxy(new URLSearchParams(params.custom_args), {
    get: (searchParams, prop) => searchParams.get(prop)
})

const code = custom_args.code;

document.getElementById("code").textContent = code

const copyButton = document.getElementById("copyButton");

function copyToClipboard(text) {
    if (window.clipboardData && window.clipboardData.setData) {
        // IE specific code path to prevent textarea being shown while dialog is visible.
        return clipboardData.setData('Text', text);

    }
    if (document.queryCommandSupported && document.queryCommandSupported('copy')) {
        const textarea = document.createElement('textarea');
        textarea.textContent = text;
        textarea.style.position = 'fixed'; // Prevent scrolling to bottom of page in MS Edge.
        document.body.append(textarea);
        textarea.select();
        try {
            return document.execCommand('copy'); // Security exception may be thrown by some browsers.
        } catch (error) {
            console.warn('Copy to clipboard failed.', error);
            return false;
        } finally {
            document.body.removeChild(textarea);
        }
    }

}

copyButton.addEventListener("click", event => {
    copyToClipboard(code)
})

