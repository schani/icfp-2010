function [m] = txt_to_m(txt, dim)

stxt = '';
for i = 1 : size(txt,1),
    stxt = [stxt ' ' txt(i,:)];
end;
    
[n1, n2, n3, mx] = regexp(stxt, '[0-9.]+');

if numel(mx) < dim*dim,
    m = NaN;
    return;
end;

for i = 1 : dim,
    for j = 1 : dim,
        m(i,j) = str2double(mx((i-1)*dim+j));
    end;
end;

