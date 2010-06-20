function [txt] = m_to_txt (m, dim)

txt = '';
for i = 1 : dim,
    for j = 1 : dim,
        if j ~= dim
            s = sprintf('%3d ',m(i,j));
        elseif i ~= dim
            s = sprintf('%3d\n',m(i,j));
        else
            s = sprintf('%3d',m(i,j));
        end;
        txt = [txt s];
    end;
end;


